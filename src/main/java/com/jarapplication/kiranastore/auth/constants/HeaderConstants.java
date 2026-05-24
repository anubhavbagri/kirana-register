package com.jarapplication.kiranastore.auth.constants;

/**
 * HEADER CONSTANTS: HTTP Header Names for Token Refresh Endpoint
 *
 * WHAT IT DOES:
 * ├─ Defines HTTP header names used by RefreshController
 * └─ Ensures consistent header naming between controller and client
 *
 * WHERE USED:
 * ├─ AUTHORIZATION → RefreshController.refreshAccessToken() → @RequestHeader("Authorization")
 * │   └─ Client sends: Authorization: Bearer <accessToken>
 * │
 * └─ REFRESH_TOKEN → RefreshController.refreshAccessToken() → @RequestHeader("Refresh-Token")
 *    └─ Client sends: Refresh-Token: <refreshToken>
 *    └─ Custom header (not standard HTTP) → specific to this application
 *
 * NOTE: AUTHORIZATION is duplicated in constants/SecurityConstants.java
 *       Consider consolidating to avoid inconsistency.
 */
public class HeaderConstants {
    public static final String AUTHORIZATION = "Authorization";   // ← Standard HTTP header
    public static final String REFRESH_TOKEN = "Refresh-Token";   // ← Custom header
}
