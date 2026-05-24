package com.jarapplication.kiranastore.feature_users.service;

import static com.jarapplication.kiranastore.feature_users.constants.LogConstants.USER_NAME_OR_PASSWORD_IS_NULL;

import com.jarapplication.kiranastore.auth.models.RefreshTokenModel;
import com.jarapplication.kiranastore.auth.service.RefreshTokenServiceImp;
import com.jarapplication.kiranastore.feature_users.models.AuthResponse;
import com.jarapplication.kiranastore.utils.JwtUtil;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Slf4j // SLF4J logger injection: Lombok logger auto-creation
@Service // Marks this class as Business Logic Layer: Creates bean + semantic clarity (“this does business logic”)
public class AuthServiceImp implements AuthService {

    // immutable (can't be changed after construction) + thread-safe
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final RefreshTokenServiceImp refreshTokenService;

    @Autowired // Explicitly tells Spring: “Use this constructor for dependency injection”
    public AuthServiceImp(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService,
            RefreshTokenServiceImp refreshTokenService) { // constructor injection
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticates the user
     *
     * @param username
     * @param password
     * @return
     */
    @Override
    public AuthResponse authenticate(String username, String password) {
        log.info("authenticate " + username);
        // Step 2a: Null check
        if (username == null || password == null) {
            throw new IllegalArgumentException(USER_NAME_OR_PASSWORD_IS_NULL);
        }
        // Step 2b: The CRITICAL line — authenticate credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        // Step 2c: Get roles from database (ONLY if auth succeeded)
        List<String> roles = userService.getUserRolesByUsername(username);

         // Step 2d: Get userId from database
        String userId = userService.getUserIdByUsername(username);

         // Step 2e: Save refresh token to MongoDB
        RefreshTokenModel refreshTokenModel = refreshTokenService.saveRefreshToken(userId);

        // Step 2f: Generate JWT token
        String jwtToken =
                jwtUtil.generateToken(username, roles, userId, refreshTokenModel.getSessionId());

        // Step 2g: Return all three pieces in `data` section of ApiResponse
        return new AuthResponse(userId, jwtToken, refreshTokenModel.getRefreshToken());
    }
}

/*
Smart Approach:
    Validate credentials FIRST (fail fast if wrong password)
    Query database only if credentials valid
    Generate token
    Return
*/