package com.jarapplication.kiranastore.constants;

/**
 * SECURITY CONSTANTS: JWT Configuration and Token Parameters
 *
 * WHAT IT DOES:
 * ├─ Stores JWT signing key, expiration times, and header/claim names
 * └─ Central configuration for all JWT-related operations
 *
 * WHY IT'S NEEDED:
 * ├─ Single source of truth for security configuration
 * ├─ Change expiration time → updated everywhere
 * ├─ Consistent claim names across generation (JwtUtil) and extraction (JwtFilter)
 * └─ Header name consistency between JwtFilter and client expectations
 *
 * WHERE USED:
 * ├─ SECRET_KEY → JwtUtil: HMAC-SHA256 signing key
 * │   └─ WARNING: Should be in environment variables (not hardcoded!)
 * │   └─ In production: Use vault, environment variable, or Spring Cloud Config
 * │
 * ├─ TOKEN_PREFIX → JwtFilter: Strips "Bearer " prefix from Authorization header
 * │   └─ Standard: Authorization: Bearer <token> (RFC 6750)
 * │
 * ├─ ACCESS_TOKEN_EXPIRATION_TIME → JwtUtil.generateToken(): 5 minutes in milliseconds
 * │   └─ 5 * 60 * 1000 = 300,000 ms = 5 minutes
 * │   └─ Short expiry: Limits damage from token theft
 * │
 * ├─ REFRESH_TOKEN_EXPIRATION_TIME → RefreshTokenServiceImp: 15 minutes
 * │   └─ 15 * 60 * 1000 = 900,000 ms = 15 minutes
 * │   └─ Longer than access token: Fewer re-logins required
 * │
 * ├─ ROLES, USER_ID, SESSION_ID → JWT custom claim names
 * │   └─ Used in JwtUtil (generation) and JwtFilter (extraction)
 * │   └─ Must match exactly between generation and extraction
 * │
 * └─ AUTHORIZATION → JwtFilter: HTTP header name for JWT token
 *    └─ Standard HTTP header: "Authorization"
 *    └─ Also defined in auth/constants/HeaderConstants (duplication)
 */
public class SecurityConstants {
    // HMAC-SHA256 signing key for JWT generation and validation
    // WARNING: MUST be at least 256 bits (32 bytes) for HS256
    // This key is 72 bytes → sufficient, but should NOT be in source code
    // Production: Use environment variable or secret vault
    public static final String SECRET_KEY =
            "yourSuperSecretKey_yourSuperSecretKey_yourSuperSecretKey_yourSuperSecretKey";

    // Standard Bearer token prefix (RFC 6750)
    // Client sends: "Authorization: Bearer eyJhbGc..."
    // JwtFilter strips "Bearer " (7 chars) to get raw token
    public static final String TOKEN_PREFIX = "Bearer ";

    // JWT access token expiration: 5 minutes (300,000 ms)
    // Short-lived: Limits damage window if token is stolen
    public static final long ACCESS_TOKEN_EXPIRATION_TIME = 5 * 60 * 1000;

    // Refresh token expiration: 15 minutes (900,000 ms)
    // Longer-lived: Allows token rotation without re-login
    public static final long REFRESH_TOKEN_EXPIRATION_TIME = 15 * 60 * 1000;

    // JWT custom claim names (must match between generation and extraction)
    public static final String ROLES = "roles";           // ← User authorization roles
    public static final String USER_ID = "userId";         // ← MongoDB user document ID
    public static final String SESSION_ID = "sessionId";   // ← Links JWT to refresh token

    // HTTP header name for JWT token (standard)
    public static final String AUTHORIZATION = "Authorization";
}
