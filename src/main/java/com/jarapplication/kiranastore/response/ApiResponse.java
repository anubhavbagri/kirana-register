package com.jarapplication.kiranastore.response;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * GENERIC API RESPONSE WRAPPER: Standardized Response Format
 *
 * WHAT IT DOES:
 * ├─ Wraps every API response (success or error) in consistent format
 * ├─ Eliminates inconsistent response formats across endpoints
 * └─ Provides predictable client-side parsing
 *
 * WHY UNIFIED RESPONSE:
 * ├─ Client consistency: Always expect same JSON structure
 * ├─ Error handling: Client knows where to find error message
 * ├─ Status tracking: Single boolean field for success/failure
 * ├─ Extensibility: Can add fields without breaking clients
 * └─ Logging: Standardized format for monitoring/analytics
 *
 * EXAMPLE USAGE ACROSS API:
 * ├─ SUCCESS Response (login endpoint):
 * │   {
 * │       "success": true,
 * │       "data": {
 * │           "userId": "user123",
 * │           "accessToken": "eyJhbGc...",
 * │           "refreshToken": "eyJhbGc..."
 * │       },
 * │       "status": "OK"
 * │   }
 * │
 * ├─ ERROR Response (duplicate username):
 * │   {
 * │       "success": false,
 * │       "status": "error",
 * │       "errorMessage": "Username already registered",
 * │       "errorCode": "USER_EXISTS"
 * │   }
 * │
 * └─ RATE LIMIT Response:
 *    {
 *        "success": false,
 *        "status": "429",
 *        "errorMessage": "Too many requests. Please try again later."
 *    }
 *
 * FIELD BREAKDOWN:
 * ├─ success (boolean):
 * │   ├─ Default: true (for convenience)
 * │   ├─ true: No exception, normal flow
 * │   ├─ false: Exception thrown, check errorMessage
 * │   └─ Why boolean? Faster than parsing status strings
 * │
 * ├─ data (Object):
 * │   ├─ Payload for successful responses
 * │   ├─ Type: Generic Object (allows any type)
 * │   ├─ Examples:
 * │   │   ├─ AuthResponse (login)
 * │   │   ├─ List<Product> (products list)
 * │   │   ├─ TransactionDto (single transaction)
 * │   │   └─ String (simple result)
 * │   └─ null for error responses
 * │
 * ├─ status (String):
 * │   ├─ HTTP-like status or custom code
 * │   ├─ "OK" = HTTP 200
 * │   ├─ "429" = Rate limit exceeded
 * │   ├─ "error" = Generic error
 * │   └─ Custom codes: "INVALID_TOKEN", "UNAUTHORIZED"
 * │
 * ├─ error (String):
 * │   ├─ Brief error type
 * │   ├─ Examples: "BadCredentials", "TokenExpired"
 * │   └─ Rarely used (errorMessage more detailed)
 * │
 * ├─ errorMessage (Object):
 * │   ├─ Detailed error description
 * │   ├─ Shown to users in UI
 * │   ├─ Examples:
 * │   │   ├─ "Username already registered"
 * │   │   ├─ "Invalid credentials"
 * │   │   └─ "Token expired. Please login again."
 * │   └─ Type Object: Can be String or nested object
 * │      └─ Flexibility for complex errors
 * │
 * └─ errorCode (String):
 * │   ├─ Machine-readable error identifier
 * │   ├─ Used for client-side logic/translations
 * │   ├─ Examples:
 * │   │   ├─ "10002" = Invalid request body
 * │   │   ├─ "USER_ALREADY_EXISTS" = Duplicate user
 * │   │   └─ "RATE_LIMIT_EXCEEDED" = Too many requests
 * │   └─ Why codes? Supports i18n (internationalization)
 * │      └─ Client maps code to localized message
 * │      └─ "USER_ALREADY_EXISTS" → Spanish: "Usuario ya existe"
 *
 * LOMBOK @Data:
 * ├─ Auto-generates:
 * │   ├─ Getters for all fields
 * │   ├─ Setters for all fields
 * │   ├─ equals() method
 * │   ├─ hashCode() method
 * │   └─ toString() method
 * │
 * ├─ Without @Data (boilerplate):
 * │   public boolean isSuccess() { return success; }
 * │   public void setSuccess(boolean success) { this.success = success; }
 * │   // ... repeated 6 times for 6 fields
 * │   public boolean equals(Object o) { ... }  // 30+ lines
 * │
 * └─ With @Data: Zero boilerplate, all methods generated
 *
 * @Component WHY:
 * ├─ Marks ApiResponse as Spring bean
 * ├─ Can be @Autowired if needed (rare)
 * ├─ More commonly: new ApiResponse() in controllers
 * └─ Consider: Probably unnecessary for DTO class
 *    └─ DTOs rarely benefit from Spring bean management
 *    └─ Better: Remove @Component (Spring anti-pattern)
 *
 * CLIENT-SIDE PARSING EXAMPLE (JavaScript):
 * ├─ Success:
 * │   const response = await fetch("/api/login", {...});
 * │   const json = await response.json();
 * │   if (json.success) {
 * │       localStorage.token = json.data.accessToken;
 * │   }
 * │
 * └─ Error:
 * │   if (!json.success) {
 * │       alert(json.errorMessage);  // Show user message
 * │       if (json.status === "429") {
 * │           // Implement backoff
 * │       }
 * │   }
 *
 * BEST PRACTICES:
 * ├─ ✓ Always wrap responses in ApiResponse
 * ├─ ✓ Set success=true only if no exception
 * ├─ ✓ Include errorCode for client logic
 * ├─ ✓ Include errorMessage for user display
 * ├─ ✗ DON'T: Return different response types from same endpoint
 * ├─ ✗ DON'T: Leak internal stack traces to client
 * └─ ✗ DON'T: Return success=true if business logic failed
 */
@Component
@Data
public class ApiResponse {

    private boolean success = true;
    private Object data;
    private String status;
    private String error;
    private Object errorMessage;
    private String errorCode;
}
