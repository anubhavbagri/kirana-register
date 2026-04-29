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

@Component
public class JwtUtil {
    private final Key SECRET_KEY = Keys.hmacShaKeyFor(SecurityConstants.SECRET_KEY.getBytes());

    /**
     * Generates JWT token
     *
     * @param username
     * @param roles
     * @param userId
     * @return
     */
    public String generateToken(
            String username, List<String> roles, String userId, String sessionId) {
        return Jwts.builder()
                .setSubject(username)
                .claim(ROLES, roles)
                .claim(USER_ID, userId)
                .claim(SESSION_ID, sessionId)
                .setIssuedAt(new Date())
                .setExpiration(
                        new Date(
                                System.currentTimeMillis()
                                        + SecurityConstants.ACCESS_TOKEN_EXPIRATION_TIME))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Validates JWT token
     *
     * @param token
     * @return
     */
    public boolean isValidateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {

            return false;
        }
    }

    /**
     * Extracts Roles from JWT token
     *
     * @param token
     * @return
     */
    public List<String> extractRoles(String token) {
        return extractClaim(token, claims -> claims.get(ROLES, List.class));
    }

    /**
     * Extracts user id from JWT token
     *
     * @param token
     * @return
     */
    public String extractUserId(String token) {
        return extractClaim(token, claims -> claims.get(USER_ID, String.class));
    }

    /**
     * Extracts session id from JWT token
     *
     * @param token
     * @return
     */
    public String extractSessionId(String token) {
        return extractClaim(token, claims -> claims.get(SESSION_ID, String.class));
    }

    /**
     * Extracts username from JWT token
     *
     * @param token
     * @return
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts expiration time from JWT token
     *
     * @param token
     * @return
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Checks if the token is expired
     *
     * @param token
     * @return
     */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts claims from JWT token
     *
     * @param token
     * @param claimsResolver
     * @return
     * @param <T>
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims =
                Jwts.parserBuilder()
                        .setSigningKey(SECRET_KEY)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();
        return claimsResolver.apply(claims);
    }
}
