package com.jarapplication.kiranastore.feature_users.controllers;

import com.jarapplication.kiranastore.AOP.annotation.RateLimiter;
import com.jarapplication.kiranastore.feature_users.models.UserRequest;
import com.jarapplication.kiranastore.feature_users.service.AuthService;
import com.jarapplication.kiranastore.feature_users.service.AuthServiceImp;
import com.jarapplication.kiranastore.feature_users.service.UserServiceImp;
import com.jarapplication.kiranastore.response.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * USER CONTROLLER: REST API for Authentication & User Registration
 *
 * WHAT IT DOES:
 * ├─ Exposes PUBLIC endpoints for user authentication:
 * │   ├─ POST /login → Authenticate user → return JWT + refresh token
 * │   └─ POST /register → Create new user account
 * ├─ Both endpoints are PUBLIC (no JWT required)
 * │   └─ Configured in SecurityConfig: .requestMatchers("/login", "/register").permitAll()
 * └─ Returns responses in standardized ApiResponse wrapper
 *
 * WHY IT'S NEEDED:
 * ├─ Entry point: Users must register/login before accessing protected endpoints
 * ├─ Token issuance: Login returns JWT (access token) + refresh token
 * ├─ Account creation: Registration creates user in MongoDB with hashed password
 * └─ Rate limiting: Login is rate-limited (5 req/min) to prevent brute-force attacks
 *
 * SECURITY:
 * ├─ /login:
 * │   ├─ @RateLimiter(limit=5) → Max 5 login attempts per minute
 * │   ├─ Prevents brute-force password guessing
 * │   ├─ AuthenticationManager validates credentials (BCrypt comparison)
 * │   └─ If valid → generates JWT + refresh token → returns AuthResponse
 * │
 * └─ /register:
 *    ├─ No rate limiting (could add for production)
 *    ├─ Checks duplicate usernames (UserNameExistsException if taken)
 *    ├─ Hashes password with BCrypt before saving
 *    └─ @CapitalizeMethod on UserServiceImp.save() uppercases username
 *
 * AUTHENTICATION FLOW:
 * ├─ 1. Client sends: POST /login { "username": "john", "password": "pass123" }
 * ├─ 2. Controller calls: authService.authenticate("john", "pass123")
 * ├─ 3. AuthServiceImp:
 * │   ├─ Validates credentials via AuthenticationManager (Spring Security)
 * │   ├─ Fetches user roles from DB
 * │   ├─ Generates JWT access token (5 min expiry)
 * │   ├─ Generates refresh token (15 min expiry, stored in MongoDB)
 * │   └─ Returns AuthResponse(userId, accessToken, refreshToken)
 * ├─ 4. Controller wraps in ApiResponse
 * └─ 5. Client receives: { "data": { "userId": "...", "accessToken": "eyJ...", "refreshToken": "..." } }
 *
 * @RestController: @Controller + @ResponseBody (JSON responses)
 * @RequestMapping: Empty = "/" (base URL is root)
 */
@RestController // ← Marks as REST API controller (auto JSON serialization)
@RequestMapping // ← Base URL = "/" (login and register are at root level)
public class UserController {

    private final UserServiceImp userService;   // ← User registration logic
    private final AuthService authService;       // ← Authentication logic

    @Autowired // ← Spring injects UserServiceImp and AuthServiceImp beans
    public UserController(UserServiceImp userService, AuthServiceImp authServiceImp) {
        this.userService = userService;
        this.authService = authServiceImp;
    }

    /**
     * LOGIN ENDPOINT: Authenticates User and Returns JWT Tokens
     *
     * HTTP: POST /login
     *
     * RATE LIMITING:
     * ├─ @RateLimiter(limit=5): Max 5 login attempts per minute
     * ├─ Prevents brute-force password attacks
     * ├─ Uses token bucket algorithm (Bucket4j via RateLimiterAspect)
     * └─ 6th attempt → RateLimitExceededException → 429 error response
     *
     * FLOW:
     * ├─ 1. Deserialize JSON body → UserRequest (username + password)
     * ├─ 2. authService.authenticate() validates credentials
     * ├─ 3. If valid → AuthResponse with userId + accessToken + refreshToken
     * ├─ 4. If invalid → BadCredentialsException → caught by ExceptionController
     * └─ 5. Client stores tokens for subsequent authenticated requests
     *
     * @param userRequest ← JSON: { "username": "john", "password": "pass123" }
     * @return ApiResponse with AuthResponse data (tokens + userId)
     */
    @RateLimiter(limit = 5) // ← AOP: 5 login attempts per minute max
    @PostMapping("/login")  // ← HTTP POST /login
    public ApiResponse login(@RequestBody UserRequest userRequest) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.ACCEPTED.name()); // ← "ACCEPTED"
        apiResponse.setData(
                authService.authenticate(userRequest.getUsername(), userRequest.getPassword()));
        return apiResponse;
    }

    /**
     * REGISTER ENDPOINT: Creates New User Account
     *
     * HTTP: POST /register
     *
     * FLOW:
     * ├─ 1. Deserialize JSON body → UserRequest (username + password + roles)
     * ├─ 2. userService.save() → @CapitalizeMethod uppercases username via AOP
     * ├─ 3. Checks if username already exists → UserNameExistsException if duplicate
     * ├─ 4. Hashes password with BCrypt → saves to MongoDB "users" collection
     * └─ 5. Returns saved UserRequest (with uppercase username, hashed password)
     *
     * AOP FLOW (username capitalization):
     * ├─ UserServiceImp.save() has @CapitalizeMethod annotation
     * ├─ CapitalizeAspect intercepts → finds @Capitalize on UserRequest.username
     * ├─ Mutates: "john" → "JOHN"
     * └─ Method proceeds with uppercase username → saves "JOHN" to MongoDB
     *
     * @param userRequest ← JSON: { "username": "john", "password": "pass123", "roles": ["USER"] }
     * @return ApiResponse with saved UserRequest data
     */
    @PostMapping("/register") // ← HTTP POST /register
    public ApiResponse register(@RequestBody UserRequest userRequest) {
        UserRequest savedUserRequest = userService.save(userRequest);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.CREATED.name()); // ← "CREATED" (201)
        apiResponse.setData(savedUserRequest);
        return apiResponse;
    }
}
