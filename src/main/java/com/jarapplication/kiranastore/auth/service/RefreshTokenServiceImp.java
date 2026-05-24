package com.jarapplication.kiranastore.auth.service;

import static com.jarapplication.kiranastore.auth.constants.LogConstants.AUTHENTICATION_FAILED;
import static com.jarapplication.kiranastore.auth.constants.LogConstants.INVALID_ACCESS_TOKEN;
import static com.jarapplication.kiranastore.constants.SecurityConstants.REFRESH_TOKEN_EXPIRATION_TIME;
import static com.jarapplication.kiranastore.constants.SecurityConstants.TOKEN_PREFIX;

import com.jarapplication.kiranastore.auth.dao.RefreshTokenDAO;
import com.jarapplication.kiranastore.auth.entity.RefreshToken;
import com.jarapplication.kiranastore.auth.models.RefreshTokenModel;
import com.jarapplication.kiranastore.feature_users.models.AuthResponse;
import com.jarapplication.kiranastore.utils.JwtUtil;
import java.util.*;
import javax.naming.AuthenticationException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * REFRESH TOKEN SERVICE IMPLEMENTATION: Token Persistence & Rotation Logic
 *
 * WHAT IT DOES:
 * ├─ saveRefreshToken(): Generates UUID → BCrypt hashes → saves to MongoDB
 * ├─ generateAccessToken(): Validates refresh token → issues new JWT access token
 * └─ Manages the refresh token lifecycle (creation + validation)
 *
 * WHY IT'S NEEDED:
 * ├─ JWT access tokens are short-lived (5 min) → need a way to refresh without re-login
 * ├─ Refresh tokens are longer-lived (15 min) and stored in MongoDB
 * ├─ Token rotation: Old access token → new access token (without password)
 * └─ Security: Refresh tokens are BCrypt hashed in DB (like passwords)
 *
 * REFRESH TOKEN SECURITY:
 * ├─ UUID.randomUUID(): Generates cryptographically strong random token
 * ├─ BCryptPasswordEncoder.encode(token): Hashes before MongoDB storage
 * │   └─ If DB is compromised: Attacker can't use hashed tokens
 * ├─ Client receives UNHASHED token (used once for refresh)
 * └─ On refresh: Server re-hashes client's token → compares with stored hash
 *
 * SESSION ID CONCEPT:
 * ├─ Each refresh token gets a MongoDB document ID (sessionId)
 * ├─ sessionId is embedded in the JWT access token
 * ├─ On token refresh:
 * │   ├─ Parse expired access token → extract sessionId
 * │   ├─ Look up MongoDB by sessionId → find refresh token document
 * │   ├─ Verify refresh token matches
 * │   └─ Issue new access token with SAME sessionId
 * └─ Benefits: Links access token to refresh token session
 *
 * TOKEN FLOW:
 * ├─ 1. Login → saveRefreshToken():
 * │   ├─ Generate UUID: "a1b2c3d4-..."
 * │   ├─ Hash: "$2a$10$..." → store in MongoDB
 * │   ├─ Set timeout: now + 15 min
 * │   └─ Return: RefreshTokenModel(hashedToken, sessionId)
 * │
 * └─ 2. Refresh → generateAccessToken():
 *    ├─ Parse access token → extract sessionId
 *    ├─ Lookup MongoDB by sessionId → get stored hash + timeout
 *    ├─ Compare client's token vs stored hash
 *    ├─ Check timeout hasn't expired
 *    └─ Generate new JWT with same claims + same sessionId
 *
 * @Component: Spring bean
 */
@Component
public class RefreshTokenServiceImp implements RefreshTokenService {
    private final RefreshTokenDAO refreshTokenDao;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    @Autowired
    public RefreshTokenServiceImp(RefreshTokenDAO refreshTokenDao, JwtUtil jwtUtil) {
        this.refreshTokenDao = refreshTokenDao;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Creates and saves a new refresh token for a user.
     *
     * FLOW:
     * ├─ 1. Generate random UUID token
     * ├─ 2. BCrypt hash it (never store plaintext)
     * ├─ 3. Create MongoDB document with: userId, tokenHash, createdAt, timeout
     * ├─ 4. Save to MongoDB "refreshToken" collection
     * └─ 5. Return model with HASHED token + session ID
     *
     * NOTE (line 55): Returns refreshToken.getToken() which is the HASHED version.
     *   └─ The unhashed UUID (variable `token`) is NOT returned to the client.
     *   └─ This means the client receives the BCrypt hash, not the UUID.
     *   └─ Potential issue: Client can't use hashed token for refresh comparison.
     *   └─ Verify this against generateAccessToken() comparison logic.
     *
     * @param userId ← MongoDB user ID (from authentication)
     * @return RefreshTokenModel(hashedToken, sessionId)
     */
    @Override
    public RefreshTokenModel saveRefreshToken(String userId) {
        // 1. Generate random UUID (cryptographically strong)
        String token = UUID.randomUUID().toString();
        // 2. BCrypt hash it (security: never store plaintext tokens)
        String tokenHash = encoder.encode(token);
        // 3. Create MongoDB document
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setToken(tokenHash);               // ← Store HASHED token
        refreshToken.setCreatedAt(new Date());
        refreshToken.setTimeout(                          // ← Expiry: now + 15 minutes
                new DateTime(new Date()).plus(REFRESH_TOKEN_EXPIRATION_TIME).toDate());
        // 4. Save to MongoDB
        refreshToken = refreshTokenDao.save(refreshToken);

        // 5. Return hashed token + session ID (MongoDB document ID)
        return new RefreshTokenModel(refreshToken.getToken(), refreshToken.getId());
    }

    /**
     * Validates refresh token and generates a new JWT access token.
     *
     * FLOW:
     * ├─ 1. Strip "Bearer " prefix from access token
     * ├─ 2. Validate access token (signature check — may be expired but parseable)
     * ├─ 3. Extract sessionId from access token claims
     * ├─ 4. Lookup MongoDB by sessionId → get stored refresh token document
     * ├─ 5. Verify: refresh token hash matches + timeout not expired
     * ├─ 6. Extract claims from access token (username, roles, userId)
     * ├─ 7. Generate NEW access token with same claims + same sessionId
     * └─ 8. Return AuthResponse(userId, newAccessToken, refreshToken)
     *
     * SECURITY CHECKS:
     * ├─ Invalid access token → AuthenticationException("Invalid access token")
     * ├─ Session not found in MongoDB → AuthenticationException
     * ├─ Refresh token hash mismatch → AuthenticationException
     * └─ Refresh token timeout expired → AuthenticationException
     *
     * NOTE: encoder.encode(refreshToken) on line 88 creates a NEW hash.
     *   └─ BCrypt generates different hash each time (random salt).
     *   └─ .equals(tokenHash) comparison will ALWAYS FAIL with BCrypt.
     *   └─ Should use: encoder.matches(refreshToken, storedHash) instead.
     *   └─ This is likely a BUG → token refresh may never succeed.
     *
     * @param refreshToken ← Client's refresh token (from Refresh-Token header)
     * @param accessToken  ← Client's access token (from Authorization header)
     * @return AuthResponse with new access token
     * @throws AuthenticationException if any validation fails
     */
    @Override
    public AuthResponse generateAccessToken(String refreshToken, String accessToken)
            throws AuthenticationException {
        // Strip "Bearer " prefix
        accessToken = accessToken.replace(TOKEN_PREFIX, "");
        // Validate access token signature (may fail if token is truly invalid)
        if (!jwtUtil.isValidateToken(accessToken)) {
            throw new AuthenticationException(INVALID_ACCESS_TOKEN);
        }
        // Extract sessionId from JWT claims → used to find refresh token in MongoDB
        String sessionId = jwtUtil.extractSessionId(accessToken);
        // Lookup refresh token document by sessionId (MongoDB document ID)
        Optional<RefreshToken> refreshTokenEntity = refreshTokenDao.findById(sessionId);
        // NOTE: This creates a NEW BCrypt hash → comparison with .equals() will fail
        // FIX: Use encoder.matches(refreshToken, storedHash) for BCrypt comparison
        String tokenHash = encoder.encode(refreshToken);
        // Validate: exists + hash matches + not expired
        if (refreshTokenEntity.isEmpty()
                || !refreshTokenEntity.get().getToken().equals(tokenHash) // ← BUG: see NOTE above
                || !refreshTokenEntity.get().getTimeout().after(new Date())) {
            throw new AuthenticationException(AUTHENTICATION_FAILED);
        }
        // Extract claims from access token for new token generation
        String username = jwtUtil.extractUsername(accessToken);
        List<String> roles = jwtUtil.extractRoles(accessToken);
        String userId = jwtUtil.extractUserId(accessToken);
        // Generate new access token with same claims + same sessionId
        String newAccessToken = jwtUtil.generateToken(username, roles, userId, sessionId);
        return new AuthResponse(userId, newAccessToken, refreshToken);
    }
}
