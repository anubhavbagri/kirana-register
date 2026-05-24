package com.jarapplication.kiranastore.feature_transactions.controllers;

import static com.jarapplication.kiranastore.constants.SecurityConstants.TOKEN_PREFIX;
import static com.jarapplication.kiranastore.feature_transactions.constants.Constants.AUTHORIZATION;

import com.jarapplication.kiranastore.feature_transactions.model.PurchaseRequest;
import com.jarapplication.kiranastore.feature_transactions.model.PurchaseResponse;
import com.jarapplication.kiranastore.feature_transactions.model.RefundRequest;
import com.jarapplication.kiranastore.feature_transactions.service.TransactionService;
import com.jarapplication.kiranastore.response.ApiResponse;
import com.jarapplication.kiranastore.utils.JwtUtil;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * TRANSACTION CONTROLLER: REST API Entry Point for Financial Operations
 *
 * WHAT IT DOES:
 * ├─ Exposes HTTP endpoints for purchase & refund operations
 * ├─ Extracts JWT token from Authorization header → identifies the user
 * ├─ Delegates business logic to TransactionService (no business logic here!)
 * └─ Wraps service responses into standardized ApiResponse format
 *
 * WHY IT'S NEEDED:
 * ├─ Separation of concerns: Controller only handles HTTP-level logic
 * │   ├─ Extracts request headers (JWT token)
 * │   ├─ Parses request body (PurchaseRequest, RefundRequest)
 * │   ├─ Delegates to service for business rules
 * │   └─ Formats HTTP response (ApiResponse + HttpStatus)
 * │
 * ├─ Security: JWT token validation happens BEFORE reaching this controller
 * │   └─ JwtFilter (in SecurityConfig) validates token → if invalid, request rejected
 * │       └─ Only valid, authenticated requests reach these endpoints
 * │
 * └─ Thin Controller pattern: Controllers should be "thin" (minimal logic)
 *    ├─ ✓ Extract data from HTTP request
 *    ├─ ✓ Call service
 *    ├─ ✓ Format response
 *    ├─ ✗ NO database calls
 *    ├─ ✗ NO business validation (that's service layer's job)
 *    └─ ✗ NO complex logic
 *
 * ANNOTATIONS:
 * ├─ @RestController:
 * │   ├─ Combines @Controller + @ResponseBody
 * │   ├─ @Controller: Marks as Spring MVC controller (handles HTTP requests)
 * │   ├─ @ResponseBody: Auto-converts return objects to JSON (Jackson serialization)
 * │   └─ Without @ResponseBody: Would look for a VIEW (HTML template) instead of JSON
 * │
 * └─ @RequestMapping("/v1/api"):
 *    ├─ Base URL prefix for all endpoints in this controller
 *    ├─ Full URLs become: /v1/api/refund, /v1/api/purchase
 *    └─ Versioning (v1): Allows future /v2/api without breaking existing clients
 *
 * REQUEST FLOW:
 * ├─ Client sends: POST /v1/api/purchase
 * │   Headers: { Authorization: "Bearer eyJhbGciOiJIUz..." }
 * │   Body: { "currencyCode": "USD", "billItems": [...] }
 * │
 * ├─ JwtFilter intercepts → validates token → sets SecurityContext
 * ├─ DispatcherServlet routes to TransactionController.purchase()
 * ├─ Controller extracts: userId from JWT, builds PurchaseRequest
 * ├─ Calls: transactionService.makePurchase(request)
 * ├─ Service processes: bill generation → currency conversion → DB save
 * └─ Controller wraps result in ApiResponse → returns HTTP 200 + JSON
 *
 * CONSTRUCTOR INJECTION (@Autowired):
 * ├─ TransactionService: Business logic for purchases & refunds
 * ├─ JwtUtil: Utility to extract userId/username from JWT tokens
 * └─ Why constructor injection? See TransactionServiceImpl.java for detailed explanation
 */
@RestController
@RequestMapping("/v1/api")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    @Autowired // ← Spring auto-injects TransactionService and JwtUtil beans at startup
    public TransactionController(TransactionService transactionService, JwtUtil jwtUtil) {
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * REFUND ENDPOINT: Reverses a Previous Purchase
     *
     * HTTP: POST /v1/api/refund
     *
     * REQUEST FLOW:
     * ├─ 1. Extract JWT from "Authorization: Bearer <token>" header
     * │      └─ token.replace("Bearer ", "") strips the prefix to get raw JWT
     * ├─ 2. Decode JWT → extract userId (who is requesting the refund)
     * ├─ 3. Extract billId from request body (which bill to refund)
     * ├─ 4. Delegate to service: validates ownership + creates refund transaction
     * └─ 5. Return success response wrapped in ApiResponse
     *
     * WHY @PostMapping (not @DeleteMapping)?
     * ├─ Refund CREATES a new transaction record (type=REFUND)
     * ├─ It doesn't delete the original purchase from DB
     * └─ POST is appropriate for "creating new resources" (even if conceptually it's a reversal)
     *
     * EXCEPTION HANDLING:
     * ├─ TransactionService may throw: RuntimeException, IllegalArgumentException
     * ├─ These bubble up to ExceptionController (global @ControllerAdvice)
     * └─ ExceptionController formats error into ApiResponse → client gets consistent error JSON
     *
     * @param token  ← JWT token from Authorization header (e.g., "Bearer eyJhbG...")
     * @param request ← JSON body containing billId to refund
     * @return ResponseEntity<ApiResponse> with refund confirmation or error
     * @throws JSONException if JWT parsing fails
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse> refund(
            @RequestHeader(AUTHORIZATION) String token, @RequestBody RefundRequest request)
            throws JSONException {
        // Strip "Bearer " prefix from token to get raw JWT string
        String jwt = token.replace(TOKEN_PREFIX, "");

        // Decode JWT to extract userId → identifies WHO is requesting the refund
        String userId = jwtUtil.extractUserId(jwt);

        // Extract billId from request body → identifies WHAT to refund
        String billId = request.getBillId();

        // Delegate to service: validates ownership, checks not already refunded, creates refund
        String result = transactionService.makeRefund(billId, userId);

        // Wrap result in standardized ApiResponse format
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(result); // ← "Refund successful" string
        apiResponse.setStatus(HttpStatus.OK.name()); // ← "OK"
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * PURCHASE ENDPOINT: Creates a New Purchase Transaction
     *
     * HTTP: POST /v1/api/purchase
     *
     * REQUEST FLOW:
     * ├─ 1. Extract JWT → get userId and userName from token
     * ├─ 2. Set userId on PurchaseRequest (client doesn't send userId, we derive it from JWT)
     * │      └─ Security: userId from JWT is tamper-proof (signed by server)
     * │         └─ If client sent userId in body, they could impersonate another user!
     * ├─ 3. Service generates bill → converts currency → saves transaction to DB
     * ├─ 4. Set userName on response (for display purposes)
     * └─ 5. Return purchase details wrapped in ApiResponse
     *
     * WHY userId FROM JWT (not request body)?
     * ├─ JWT is server-signed → client cannot forge userId
     * ├─ Even if attacker modifies request body's userId, we IGNORE it
     * └─ We overwrite with JWT-derived userId: request.setUserId(UserId)
     *
     * DATA FLOW:
     * ├─ Client sends: { "currencyCode": "USD", "billItems": [{"itemName": "Rice", "quantity": 2}] }
     * ├─ Controller adds: userId from JWT
     * ├─ Service returns: PurchaseResponse (billId, amount, items, transactionType)
     * ├─ Controller adds: userName from JWT
     * └─ Client receives: { "data": { "userName": "John", "billId": "abc", "amount": 150.0, ... } }
     *
     * @param token   ← JWT from Authorization header
     * @param request ← JSON body with currencyCode and billItems list
     * @return ApiResponse with purchase details (billId, amount, items)
     * @throws JSONException if JWT parsing fails
     */
    @PostMapping("/purchase")
    public ApiResponse purchase(
            @RequestHeader(AUTHORIZATION) String token, @RequestBody PurchaseRequest request)
            throws JSONException {

        // Extract raw JWT and decode userId + userName
        String jwt = token.replace(TOKEN_PREFIX, "");
        String UserId = jwtUtil.extractUserId(jwt);
        String userName = jwtUtil.extractUsername(jwt);

        // Set userId from JWT (secure, tamper-proof) instead of trusting client-sent userId
        request.setUserId(UserId);

        // Core business logic: bill generation → currency conversion → DB save
        PurchaseResponse response = transactionService.makePurchase(request);

        // Enrich response with userName for client display
        response.setUserName(userName);

        // Wrap in standardized ApiResponse
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(response);
        apiResponse.setStatus(HttpStatus.OK.name());
        return apiResponse;
    }
}
