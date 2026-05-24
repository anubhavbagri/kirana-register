package com.jarapplication.kiranastore.auth.service;

import com.jarapplication.kiranastore.auth.models.RefreshTokenModel;
import com.jarapplication.kiranastore.feature_users.models.AuthResponse;
import javax.naming.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * REFRESH TOKEN SERVICE INTERFACE: Contract for Token Lifecycle Management
 *
 * WHAT IT DOES:
 * ├─ Defines the contract for refresh token operations
 * ├─ Implemented by RefreshTokenServiceImp
 * └─ Used by: AuthServiceImp (save token during login), RefreshController (validate + rotate)
 *
 * WHY AN INTERFACE?
 * ├─ Dependency Inversion: RefreshController depends on abstraction
 * ├─ Testability: Mock token operations in tests
 * └─ Extensibility: Could implement RedisRefreshTokenService (faster lookups)
 *
 * @Service: Marks as Spring-managed service bean
 */
@Service
public interface RefreshTokenService {
    /**
     * Creates and persists a new refresh token for a user.
     *
     * @param userId ← User's MongoDB ID
     * @return RefreshTokenModel with token hash and session ID
     */
    RefreshTokenModel saveRefreshToken(String userId);

    /**
     * Validates refresh token and generates a new JWT access token.
     *
     * @param refreshToken ← Client's refresh token (from header)
     * @param accessToken  ← Client's current access token (may be expired)
     * @return AuthResponse with new access token
     * @throws AuthenticationException if validation fails
     */
    AuthResponse generateAccessToken(String refreshToken, String accessToken)
            throws AuthenticationException;
}
