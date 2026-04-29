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

@RestController
@RequestMapping
public class UserController {

    private final UserServiceImp userService;
    private final AuthService authService;

    @Autowired
    public UserController(UserServiceImp userService, AuthServiceImp authServiceImp) {
        this.userService = userService;
        this.authService = authServiceImp;
    }

    /**
     * user login
     *
     * @param userRequest
     * @return
     */
    @RateLimiter(limit = 5)
    @PostMapping("/login")
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
    @PostMapping("/register")
    public ApiResponse register(@RequestBody UserRequest userRequest) {
        UserRequest savedUserRequest = userService.save(userRequest);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setStatus(HttpStatus.CREATED.name());
        apiResponse.setData(savedUserRequest);
        return apiResponse;
    }
}
