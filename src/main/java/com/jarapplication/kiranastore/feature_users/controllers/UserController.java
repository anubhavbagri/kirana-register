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

@RestController // Marks class as a REST API controller that handles HTTP requests and returns JSON/XML
@RequestMapping // Base URL; Empty = "/"
public class UserController {

    private final UserServiceImp userService;
    private final AuthService authService;

    @Autowired // Explicitly tells Spring: “Use this constructor for dependency injection”
    public UserController(UserServiceImp userService, AuthServiceImp authServiceImp) {
        this.userService = userService;
        this.authService = authServiceImp;
    }

    /** URL construction:
    @RequestMapping value +  @PostMapping value
      "/api/users"      +       "/login"
            ↓                      ↓
        Base path        +    Endpoint path
                    ↓
            /api/users/login
     *
     * user login
     *
     * @param userRequest
     * @return
     * @RateLimiter: custom AOP annotation- limit requests to 5 per minute, prevent brute force attacks on login
     * @PostMapping("/login") maps HTTP POST requests to `/login` endpoint to this method
     * @RequestBody tells spring to deserialize HTTP request JSON body to `UserRequest` object
     * @RestController auto-serializes ApiResponse to JSON
     */
    @RateLimiter(limit = 5)
    @PostMapping("/login") // Final URL: {BASE URL}/login
    public ApiResponse login(@RequestBody UserRequest userRequest) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.ACCEPTED.name());
        apiResponse.setData(
                authService.authenticate(userRequest.getUsername(), userRequest.getPassword()));
        return apiResponse;
    }

    /**
     * User sign up
     *
     * @param userRequest
     * @return
     */
    @PostMapping("/register") // Final URL: {BASE URL}/register
    public ApiResponse register(@RequestBody UserRequest userRequest) {
        UserRequest savedUserRequest = userService.save(userRequest);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.CREATED.name());
        apiResponse.setData(savedUserRequest);
        return apiResponse;
    }
}
