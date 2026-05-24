package com.jarapplication.kiranastore.exception;

import com.jarapplication.kiranastore.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * GLOBAL EXCEPTION HANDLER: Centralized Error Management
 *
 * WHAT IT DOES:
 * ├─ @ControllerAdvice: Catches exceptions from ANY controller (global)
 * ├─ @ExceptionHandler: Maps specific exception types to response methods
 * ├─ Standardizes error responses across entire application
 * └─ Logs all errors for debugging + monitoring
 *
 * WHY IT'S NEEDED:
 * ├─ Without it: Each controller writes try-catch blocks (boilerplate)
 * ├─ Consistency: All errors return same format (ApiResponse wrapper)
 * ├─ Maintainability: Change error response in ONE place
 * ├─ Logging: Central place to track all exceptions
 * ├─ Client-friendly: Consistent error messages + error codes
 * └─ Security: Hide internal stack traces from client (return generic messages)
 *
 * DESIGN PATTERN: CENTRALIZED ERROR HANDLING
 * ├─ Without @ControllerAdvice (BAD):
 * │   @PostMapping("/api/users")
 * │   public ApiResponse createUser(UserRequest req) {
 * │       try {
 * │           // business logic
 * │       } catch (UserNameExistsException e) {
 * │           ApiResponse response = new ApiResponse();
 * │           response.setSuccess(false);
 * │           response.setErrorMessage(e.getMessage());
 * │           return response;
 * │       } catch (IllegalArgumentException e) {
 * │           // similar error handling
 * │       }
 * │   }
 * │   // Repeated in 100 other endpoints!
 * │
 * └─ With @ControllerAdvice (GOOD):
 *     └─ Write try-catch once in this class
 *        └─ All controllers automatically use it
 *
 * HTTP STATUS CODES vs ApiResponse.status:
 * ├─ HTTP 200: Backend suggests success to Spring/browsers (conventional)
 * ├─ ApiResponse.status: Your custom field (for frontend logic)
 * ├─ DESIGN CHOICE: Return 200 with custom status field
 * │   Rationale:
 * │   ├─ Some HTTP clients only check HTTP status
 * │   ├─ 200 for all responses simplifies client logic
 * │   ├─ ApiResponse.status tells detailed error type
 * │   └─ Trade-off: Violates REST best practices but simpler implementation
 * │
 * ├─ BETTER approach (not your code):
 * │   ├─ UserNameExistsException → HTTP 409 Conflict
 * │   ├─ RateLimitExceededException → HTTP 429 Too Many Requests
 * │   ├─ IllegalArgumentException → HTTP 400 Bad Request
 * │   └─ Then client reads HTTP status code (no need for custom field)
 *
 * EXCEPTION HIERARCHY (Your Custom Exceptions):
 * ├─ RuntimeException (Unchecked)
 * │   ├─ UserNameExistsException (extends RuntimeException)
 * │   ├─ RateLimitExceededException (extends RuntimeException)
 * │   └─ Why Unchecked?
 * │      └─ Checked exceptions force try-catch everywhere (boilerplate)
 * │         Unchecked can be thrown anywhere without declaring
 * │
 * ├─ Spring Built-in exceptions also caught:
 * │   ├─ HttpMessageNotReadableException: Invalid JSON in request body
 * │   └─ MissingServletRequestParameterException: Missing @RequestParam
 *
 * EXCEPTION FLOW:
 * ├─ Client sends invalid request
 * │   └─ GET /api/users?id=  (missing id parameter)
 * │
 * ├─ Spring detects missing @RequestParam
 * │   └─ Throws MissingServletRequestParameterException
 * │
 * ├─ Exception travels up call stack
 * │   └─ DispatcherServlet catches it
 * │
 * ├─ DispatcherServlet searches for @ExceptionHandler method
 * │   └─ Finds: handleSpringRequestParamException() in this class
 * │
 * ├─ Handler executes:
 * │   ├─ Creates ApiResponse
 * │   ├─ Sets success=false, errorMessage, errorCode
 * │   └─ Returns ResponseEntity(apiResponse, HttpStatus.OK)
 * │
 * └─ Response sent to client:
 *     └─ HTTP 200 OK + JSON error details
 *
 * LOGGING (@Slf4j):
 * ├─ Lombok auto-generates logger
 * ├─ log.error("message", exception): Logs full stack trace
 * ├─ log.error("message {}", variable): Format strings
 * ├─ Why log?: Debugging + monitoring + alerting
 * └─ Logs stored in: ./logs/sample.log (configured in application.properties)
 *
 * ERROR CODE MAPPING:
 * ├─ "10002": Invalid request (body or parameter)
 * ├─ "429": Rate limit exceeded
 * ├─ "error": Generic error (fallback)
 * └─ Why codes? Clients can parse machine-readable codes → localize messages
 */
@Slf4j
@ControllerAdvice
public class ExceptionController {

    /**
     * userName doesn't exist Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = UserNameExistsException.class)
    public Object UserNameExistsException(UserNameExistsException e) {
        log.error("User Name Doesn't exist {}", e.getMessage(), e);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Illegal Argument Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Object IllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal Argument Exception {}", e.getMessage(), e);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage(e.getMessage());
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Rate limit Exceeded Exception
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = RateLimitExceededException.class)
    public Object RateLimitExceededException(RateLimitExceededException e) {
        log.error("Rate Limit Exception {}", e.getMessage(), e);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage(e.getMessage());
        apiResponse.setStatus("429");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public Object handleSpringRequestBodyException(HttpMessageNotReadableException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("10002");
        apiResponse.setErrorMessage("Invalid Request Body");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Missing param in url
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Object handleSpringRequestParamException(MissingServletRequestParameterException e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("10002");
        apiResponse.setErrorMessage("Invalid Request Parameter");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Exception for general
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    public Object handleException(Exception e) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(false);
        apiResponse.setStatus("error");
        apiResponse.setErrorMessage("Something went wrong. Please try again later!");
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
}
