package com.jarapplication.kiranastore.auth.constants;

/**
 * AUTH LOG CONSTANTS: Error Messages for Token Refresh Operations
 *
 * WHAT IT DOES:
 * ├─ Stores error messages for refresh token validation failures
 * └─ Used by RefreshTokenServiceImp.generateAccessToken()
 *
 * WHERE USED:
 * ├─ INVALID_ACCESS_TOKEN → When access token parsing/validation fails during refresh
 * │   └─ RefreshTokenServiceImp: jwtUtil.isValidateToken(accessToken) returns false
 * │
 * └─ AUTHENTICATION_FAILED → When refresh token validation fails:
 *    ├─ Session not found in MongoDB (sessionId lookup returned empty)
 *    ├─ Refresh token hash doesn't match stored hash
 *    └─ Refresh token timeout has expired
 */
public class LogConstants {
    // Access token couldn't be parsed or signature is invalid
    public static final String INVALID_ACCESS_TOKEN = "Invalid access token";

    // Refresh token validation failed (hash mismatch, expired, or session not found)
    public static final String AUTHENTICATION_FAILED = "Authentication failed";
}
