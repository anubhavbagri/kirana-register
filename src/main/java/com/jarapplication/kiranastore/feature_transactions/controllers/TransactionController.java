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

@RestController("/v1/api")
public class TransactionController {

    private final TransactionService transactionService;
    private final JwtUtil jwtUtil;

    @Autowired
    public TransactionController(TransactionService transactionService, JwtUtil jwtUtil) {
        this.transactionService = transactionService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Makes a Refund when provided with bill Id
     *
     * @param token
     * @param request
     * @return
     * @throws JSONException
     */
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse> refund(
            @RequestHeader(AUTHORIZATION) String token, @RequestBody RefundRequest request)
            throws JSONException {
        String jwt = token.replace(TOKEN_PREFIX, "");
        String userId = jwtUtil.extractUserId(jwt);
        String billId = request.getBillId();

        String result = transactionService.makeRefund(billId, userId);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(result);
        apiResponse.setStatus(HttpStatus.OK.name());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Generates a bill of required products and saves the transactions
     *
     * @param token
     * @param request
     * @return
     * @throws JSONException
     */
    @PostMapping("/purchase")
    public ApiResponse purchase(
            @RequestHeader(AUTHORIZATION) String token, @RequestBody PurchaseRequest request)
            throws JSONException {

        String jwt = token.replace(TOKEN_PREFIX, "");
        String UserId = jwtUtil.extractUserId(jwt);
        String userName = jwtUtil.extractUsername(jwt);
        request.setUserId(UserId);

        PurchaseResponse response = transactionService.makePurchase(request);

        response.setUserName(userName);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setData(response);
        apiResponse.setStatus(HttpStatus.OK.name());
        return apiResponse;
    }
}
