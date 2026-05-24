package com.jarapplication.kiranastore.utils;

import static com.jarapplication.kiranastore.constants.SecurityConstants.*;

import com.jarapplication.kiranastore.constants.SecurityConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;
import org.springframework.stereotype.Component;

/**
 * JWT UTIL: JSON Web Token Generation, Parsing, and Validation
 *
 * WHAT IT DOES:
 * ├─ Generates signed JWT access tokens with user claims
 * ├─ Validates token signatures and expiration
 * ├─ Extracts claims (username, roles, userId, sessionId) from tokens
 * └─ Central JWT utility used by JwtFilter and AuthServiceImp
 *
 * WHY IT'S NEEDED:
 * ├─ JWT is the authentication mechanism for this application
 * ├─ Tokens are self-contained (no server-side session needed)
 * ├─ Signature prevents tampering (HMAC-SHA256)
 * └─ Expiration prevents indefinite token usage
 *
 * JWT STRUCTURE:
 * ├─ Header: { "alg": "HS256", "typ": "JWT" }
 * ├─ Payload (Claims):
 * │   ├─ sub: "JOHN" (username → standard claim)
 * │   ├─ roles: ["ADMIN", "USER"] (custom claim)
 * │   ├─ userId: "6657a1b2c3d4e5f6" (custom claim)
 * │   ├─ sessionId: "session-123" (custom claim, links to refresh token)
 * │   ├─ iat: 1716582000 (issued at → standard claim)
 * │   └─ exp: 1716582300 (expiration → iat + 5 min → standard claim)
 * └─ Signature: HMAC-SHA256(header.payload, SECRET_KEY)
 *
 * TOKEN FLOW:
 * ├─ Login: generateToken() → returns JWT string → client stores it
 * ├─ API call: Client sends JWT in Authorization header
 * ├─ JwtFilter: extractUsername() + isValidateToken() → validates JWT
 * ├─ Refresh: extractSessionId() → links to refresh token in MongoDB
 * └─ Expired: Client uses refresh token to get new JWT
 *
 * SIGNING:
 * ├─ Algorithm: HMAC-SHA256 (symmetric key, server-side only)
 * ├─ Key: Derived from SecurityConstants.SECRET_KEY string
 * ├─ Keys.hmacShaKeyFor(): Converts raw bytes → cryptographic Key object
 * └─ IMPORTANT: SECRET_KEY should be in environment variables (not hardcoded!)
 *    └─ Current: Hardcoded in SecurityConstants → security risk in production
 *
 * extractClaim() PATTERN (Functional Programming):
 * ├─ Generic method: <T> T extractClaim(token, Function<Claims, T>)
 * ├─ Parses JWT once → applies claimsResolver function to extract specific claim
 * ├─ Eliminates duplication: each extract method reuses this pattern
 * ├─ Examples:
 * │   ├─ extractUsername: Claims::getSubject (standard claim)
 * │   ├─ extractRoles: claims -> claims.get("roles", List.class) (custom claim)
 * │   └─ extractExpiration: Claims::getExpiration (standard claim)
 * └─ Method references (Claims::getSubject) are syntactic sugar for lambdas
 *
 * @Component: Spring bean (injectable into JwtFilter, AuthServiceImp)
 */
@Component // ← Spring bean for JWT operations
public class JwtUtil {
    // HMAC-SHA256 signing key derived from secret string
    // WARNING: Should be in environment variables, not hardcoded
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SecurityConstants.SECRET_KEY.getBytes());

    /**
     * Generates a signed JWT access token with user claims.
     *
     * TOKEN STRUCTURE:
     * ├─ Subject: username (principal identity)
     * ├─ Custom claims: roles, userId, sessionId
     * ├─ Issued at: Current timestamp
     * ├─ Expiration: Current time + 5 minutes (ACCESS_TOKEN_EXPIRATION_TIME)
     * └─ Signed with: HMAC-SHA256 using SECRET_KEY
     *
     * @param username  ← User's username (JWT subject)
     * @param roles     ← User's roles (e.g., ["ADMIN", "USER"])
     * @param userId    ← MongoDB user document ID
     * @param sessionId ← MongoDB refresh token document ID (links access to refresh)
     * @return Signed JWT string (e.g., "eyJhbGciOiJIUzI1NiJ9.eyJzdWI...")
     */
    public String generateToken(
            String username, List<String> roles, String userId, String sessionId) {
        return Jwts.builder()
                .setSubject(username)          // ← Standard claim: who this token represents
                .claim(ROLES, roles)           // ← Custom claim: authorization roles
                .claim(USER_ID, userId)        // ← Custom claim: MongoDB user ID
                .claim(SESSION_ID, sessionId)  // ← Custom claim: links to refresh token
                .setIssuedAt(new Date())       // ← Standard claim: when issued
                .setExpiration(                // ← Standard claim: when it expires
                        new Date(
                                System.currentTimeMillis()
                                        + SecurityConstants.ACCESS_TOKEN_EXPIRATION_TIME)) // ← +5 min
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256) // ← Sign with HMAC-SHA256
                .compact(); // ← Build final JWT string (base64url encoded)
    }

    /**
     * Validates a JWT token (signature + expiration).
     *
     * VALIDATION:
     * ├─ parseClaimsJws(): Verifies HMAC-SHA256 signature
     * │   └─ If tampered: throws SignatureException
     * ├─ isTokenExpired(): Checks expiration claim
     * │   └─ If expired: returns false
     * └─ Any exception: returns false (invalid token)
     *
     * @param token ← JWT string to validate
     * @return true if valid and not expired, false otherwise
     */
    public boolean isValidateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            // Catches: SignatureException, ExpiredJwtException, MalformedJwtException, etc.
            return false;
        }
    }

    /**
     * Extracts roles from JWT custom claim.
     * @return List of role strings (e.g., ["ADMIN", "USER"])
     */
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get(ROLES, List.class));
    }

    /**
     * Extracts userId from JWT custom claim.
     * @return MongoDB user document ID string
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID, String.class));
    }

    /**
     * Extracts sessionId from JWT custom claim.
     * @return MongoDB refresh token document ID (links JWT to refresh token)
     */
    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get(SESSION_ID, String.class));
    }

    /**
     * Extracts username (subject) from JWT standard claim.
     * Uses method reference: Claims::getSubject
     * @return Username string (e.g., "JOHN")
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration date from JWT standard claim.
     * @return Date when this token expires
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the token's expiration date has passed.
     * @return true if expired, false if still valid
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Generic claim extractor using functional programming pattern.
     *
     * HOW IT WORKS:
     * ├─ 1. Parse JWT → get Claims object (all decoded claims)
     * ├─ 2. Apply claimsResolver function → extract specific claim
     * └─ 3. Return typed result
     *
     * This avoids parsing the JWT multiple times when extracting multiple claims.
     * However, each extract method currently calls this independently → parses multiple times.
     * Optimization: Parse once → extract all needed claims in one pass.
     *
     * @param token          ← JWT string
     * @param claimsResolver ← Function that extracts a specific claim from Claims
     * @param <T>            ← Return type (String, List, Date, etc.)
     * @return Extracted claim value
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(SECRET_KEY)  // ← Verify signature
                        .build()
                        .parseClaimsJws(token)      // ← Parse + validate
                        .getBody();                 // ← Get payload (claims)
        return claimsResolver.apply(claims);        // ← Apply extraction function
    }
}
