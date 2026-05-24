package com.jarapplication.kiranastore.constants;

/**
 * ROOT LOG CONSTANTS: JWT & Rate Limiting Error Messages
 *
 * WHAT IT DOES:
 * ├─ Stores error messages for JWT validation and rate limiting at the HTTP filter level
 * └─ Used by JwtFilter and RateLimiterFilter
 *
 * WHY IT'S NEEDED:
 * ├─ Consistent error messages between JwtFilter and RateLimiterFilter
 * ├─ Separate from feature-specific LogConstants (product, transaction, user)
 * └─ Root-level concerns: Authentication and rate limiting apply globally
 *
 * WHERE USED:
 * ├─ UNAUTHORIZED_NO_JWT → JwtFilter: No Authorization header or missing "Bearer " prefix
 * ├─ UNAUTHORIZED_INVALID_JWT → JwtFilter: Token signature invalid or expired
 * ├─ INVALID_OR_EXPIRED_JWT → JwtFilter: Catch-all error in outer try-catch
 * └─ TOO_MANY_REQUESTS → RateLimiterFilter: Global rate limit exceeded (100 req/min)
 */
public class LogConstants {
    // JwtFilter: No Authorization header or missing "Bearer " prefix
    public static final String UNAUTHORIZED_NO_JWT = "Unauthorized: No JWT found.";

    // JwtFilter: Token signature verification failed or token is expired
    public static final String UNAUTHORIZED_INVALID_JWT = "Unauthorized: Invalid JWT.";

    // JwtFilter: Catch-all error message for any JWT parsing/validation exception
    public static final String INVALID_OR_EXPIRED_JWT = "Invalid or Expired JWT.";

    // RateLimiterFilter: Global rate limit (100 req/min) exceeded
    public static final String TOO_MANY_REQUESTS = "Too Many Requests. ";
}
