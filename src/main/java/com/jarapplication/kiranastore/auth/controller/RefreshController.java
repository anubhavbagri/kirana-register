package com.jarapplication.kiranastore.auth.controller;

import static com.jarapplication.kiranastore.auth.constants.HeaderConstants.AUTHORIZATION;
import static com.jarapplication.kiranastore.auth.constants.HeaderConstants.REFRESH_TOKEN;

import com.jarapplication.kiranastore.auth.service.RefreshTokenService;
import com.jarapplication.kiranastore.feature_users.models.AuthResponse;
import javax.naming.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REFRESH CONTROLLER: JWT Token Rotation Endpoint
 *
 * WHAT IT DOES:
 * ├─ Exposes GET /generate-token → refreshes an expired/expiring JWT access token
 * ├─ Requires BOTH the current access token AND refresh token in HTTP headers
 * └─ Returns a new AuthResponse with a fresh access token
 *
 * WHY IT'S NEEDED:
 * ├─ JWT access tokens have SHORT expiry (5 minutes in this app)
 * ├─ Without refresh: User must re-login every 5 minutes → bad UX
 * ├─ Refresh token: Longer-lived (15 min), stored in MongoDB → use to get new access token
 * └─ Token rotation: Client sends expired access token + valid refresh token → gets new access token
 *
 * TOKEN LIFECYCLE:
 * ├─ Login (POST /login):
 * │   └─ Returns: accessToken (5 min) + refreshToken (15 min)
 * │
 * ├─ API calls (e.g., GET /products):
 * │   └─ Client sends: Authorization: Bearer <accessToken>
 * │   └─ If expired → 401 Unauthorized
 * │
 * ├─ Token refresh (GET /generate-token) [THIS ENDPOINT]:
 * │   └─ Client sends BOTH headers:
 * │       ├─ Authorization: Bearer <expiredAccessToken>
 * │       └─ Refresh-Token: <refreshToken>
 * │   └─ Returns: new accessToken (5 min) + same refreshToken
 * │
 * └─ Refresh token expired (after 15 min):
 *    └─ Must re-login via POST /login
 *
 * HTTP HEADERS:
 * ├─ Authorization: "Bearer eyJhbGc..." (current access token, may be expired)
 * │   └─ Parsed to extract: username, roles, userId, sessionId
 * │   └─ Even if expired, the claims are still readable (signature still verified)
 * │
 * └─ Refresh-Token: UUID string (the refresh token from login)
 *    └─ Compared against BCrypt hash in MongoDB
 *
 * @RestController: REST API controller with JSON responses
 * @RequestMapping: Empty = base URL "/"
 */
@RestController
@RequestMapping
public class RefreshController {
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public RefreshController(RefreshTokenService refreshTokenService) {
        this.refreshTokenService = refreshTokenService;
    }

    /**
     * Refreshes the JWT access token using the refresh token.
     *
     * FLOW:
     * ├─ 1. Extract access token from Authorization header
     * ├─ 2. Extract refresh token from Refresh-Token header
     * ├─ 3. Validate access token (parse claims, check signature)
     * ├─ 4. Look up refresh token by sessionId in MongoDB
     * ├─ 5. Compare refresh token against stored BCrypt hash
     * ├─ 6. Check refresh token not expired (timeout > now)
     * ├─ 7. Generate new access token with same claims
     * └─ 8. Return AuthResponse(userId, newAccessToken, sameRefreshToken)
     *
     * @param accessToken  ← From Authorization header (may be expired but parseable)
     * @param refreshToken ← From Refresh-Token header (UUID, must match DB hash)
     * @return AuthResponse with new access token
     * @throws AuthenticationException if tokens are invalid or expired
     */
    @GetMapping("/generate-token") // ← GET /generate-token
    public AuthResponse refreshAccessToken(
            @RequestHeader(AUTHORIZATION) String accessToken,    // ← "Bearer eyJ..."
            @RequestHeader(REFRESH_TOKEN) String refreshToken)   // ← UUID string
            throws AuthenticationException {
        return refreshTokenService.generateAccessToken(refreshToken, accessToken);
    }
}
