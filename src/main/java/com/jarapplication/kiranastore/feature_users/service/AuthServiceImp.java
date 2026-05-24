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

/**
 * AUTH SERVICE IMPLEMENTATION: Core Authentication Logic (Login Flow)
 *
 * WHAT IT DOES:
 * ├─ Validates user credentials via Spring Security AuthenticationManager
 * ├─ Generates JWT access token (5 min expiry) with user claims
 * ├─ Creates + saves refresh token (15 min expiry) to MongoDB
 * └─ Returns AuthResponse with userId + accessToken + refreshToken
 *
 * WHY IT'S NEEDED:
 * ├─ Orchestrates the full login workflow:
 * │   ├─ Credential validation (BCrypt comparison)
 * │   ├─ Role fetching (from MongoDB)
 * │   ├─ Token generation (JWT signing)
 * │   └─ Refresh token persistence (MongoDB)
 * ├─ Separation: Controller doesn't know auth details
 * └─ Fail-fast: Validates credentials BEFORE expensive DB queries
 *
 * AUTHENTICATION FLOW (Step by Step):
 * ├─ Step 1: Null check (fail fast)
 * │   └─ If username or password is null → IllegalArgumentException
 * │
 * ├─ Step 2: AuthenticationManager.authenticate()
 * │   ├─ Spring Security's core authentication
 * │   ├─ Internally calls: CustomUserDetailsService.loadUserByUsername()
 * │   ├─ Compares: BCrypt(inputPassword) vs storedPasswordHash
 * │   ├─ If match: Returns Authentication object (success)
 * │   └─ If no match: Throws BadCredentialsException
 * │       └─ Caught by ExceptionController → error response
 * │
 * ├─ Step 3: Fetch roles from DB (ONLY if auth succeeded)
 * │   └─ userService.getUserRolesByUsername() → ["ADMIN", "USER"]
 * │
 * ├─ Step 4: Fetch userId from DB
 * │   └─ userService.getUserIdByUsername() → "user-123"
 * │
 * ├─ Step 5: Save refresh token to MongoDB
 * │   └─ refreshTokenService.saveRefreshToken(userId)
 * │   └─ Returns: RefreshTokenModel(refreshToken, sessionId)
 * │
 * ├─ Step 6: Generate JWT access token
 * │   └─ jwtUtil.generateToken(username, roles, userId, sessionId)
 * │   └─ JWT contains: sub=username, roles, userId, sessionId, exp=5min
 * │
 * └─ Step 7: Return AuthResponse
 *    └─ { userId, accessToken (JWT), refreshToken (UUID) }
 *
 * WHY FAIL-FAST PATTERN?
 * ├─ Validate credentials FIRST (step 2)
 * ├─ Only query DB for roles/userId AFTER auth succeeds (steps 3-4)
 * └─ Saves DB queries if password is wrong (most common failure case)
 *
 * @Slf4j (Lombok): Auto-generates logger for info/error logging
 * @Service: Spring bean with business logic semantic
 */
@Slf4j    // ← Lombok: Auto-generates `log` field (Logger)
@Service  // ← Spring bean
public class AuthServiceImp implements AuthService {

    private final AuthenticationManager authenticationManager; // ← Spring Security auth core
    private final JwtUtil jwtUtil;                              // ← JWT generation/validation
    private final UserService userService;                      // ← User role/ID lookups
    private final RefreshTokenServiceImp refreshTokenService;   // ← Refresh token persistence

    @Autowired
    public AuthServiceImp(
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserService userService,
            RefreshTokenServiceImp refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Authenticates user credentials and returns JWT + refresh tokens.
     *
     * @param username ← User's username (will be looked up as UPPERCASE in DB)
     * @param password ← User's plaintext password (compared via BCrypt)
     * @return AuthResponse with userId, JWT access token, and refresh token
     * @throws IllegalArgumentException if username or password is null
     * @throws org.springframework.security.authentication.BadCredentialsException if password wrong
     */
    @Override
    public AuthResponse authenticate(String username, String password) {
        log.info("authenticate " + username); // ← Audit log: who is trying to login

        // Step 1: Null check (fail fast)
        if (username == null || password == null) {
            throw new IllegalArgumentException(USER_NAME_OR_PASSWORD_IS_NULL);
        }

        // Step 2: The CRITICAL line — validate credentials
        // Internally: loads user via CustomUserDetailsService → BCrypt password match
        // Throws BadCredentialsException if password is wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        // Step 3: Get roles from database (ONLY if auth succeeded)
        List<String> roles = userService.getUserRolesByUsername(username);

        // Step 4: Get userId from database
        String userId = userService.getUserIdByUsername(username);

        // Step 5: Save refresh token to MongoDB (BCrypt hashed)
        RefreshTokenModel refreshTokenModel = refreshTokenService.saveRefreshToken(userId);

        // Step 6: Generate JWT access token with all claims
        String jwtToken =
                jwtUtil.generateToken(username, roles, userId, refreshTokenModel.getSessionId());

        // Step 7: Return all three pieces → client stores for subsequent requests
        return new AuthResponse(userId, jwtToken, refreshTokenModel.getRefreshToken());
    }
}