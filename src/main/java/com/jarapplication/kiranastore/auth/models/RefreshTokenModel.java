package com.jarapplication.kiranastore.auth.models;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * REFRESH TOKEN MODEL: DTO for Refresh Token Data Transfer
 *
 * WHAT IT DOES:
 * ├─ Carries refresh token data between service layers
 * ├─ Created by: RefreshTokenServiceImp.saveRefreshToken()
 * └─ Used by: AuthServiceImp to extract sessionId for JWT generation
 *
 * FIELDS:
 * ├─ refreshToken: The BCrypt-hashed refresh token string
 * │   └─ Returned to client as part of AuthResponse (login response)
 * │   └─ Client sends this back during token refresh
 * │
 * └─ sessionId: MongoDB document ID of the RefreshToken entity
 *    └─ Embedded as "sessionId" claim in the JWT access token
 *    └─ Links JWT to its corresponding refresh token in MongoDB
 *    └─ On refresh: JWT sessionId → MongoDB lookup → validate refresh token
 *
 * @Data (Lombok): Generates getters/setters/equals/hashCode/toString
 * @AllArgsConstructor (Lombok): Generates constructor with all fields as parameters
 *   └─ new RefreshTokenModel(refreshToken, sessionId)
 */
@Data           // ← Lombok: getter/setter/equals/hashCode/toString
@AllArgsConstructor // ← Lombok: Constructor with all fields
public class RefreshTokenModel {
    private String refreshToken; // ← BCrypt hash of the refresh token
    private String sessionId;    // ← MongoDB document ID (links JWT to refresh token)
}
