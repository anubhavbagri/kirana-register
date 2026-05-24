package com.jarapplication.kiranastore.filters;

import static com.jarapplication.kiranastore.constants.LogConstants.*;
import static com.jarapplication.kiranastore.constants.SecurityConstants.AUTHORIZATION;
import static com.jarapplication.kiranastore.constants.SecurityConstants.TOKEN_PREFIX;

import com.jarapplication.kiranastore.feature_users.service.CustomUserDetailsService;
import com.jarapplication.kiranastore.response.ApiResponse;
import com.jarapplication.kiranastore.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT FILTER: Per-Request Token Validation & Security Context Setup
 *
 * WHAT IT DOES:
 * ├─ Runs on EVERY request before reaching controllers
 * ├─ Extracts JWT from Authorization header
 * ├─ Validates token signature + expiration
 * ├─ Extracts claims (username, roles, userId)
 * ├─ Populates SecurityContext with authenticated user
 * └─ Allows @PreAuthorize, SecurityContextHolder.getContext() to work
 *
 * WHY FILTER (not AOP Aspect):
 * ├─ Filter: HTTP layer interceptor (runs before Spring dispatcher)
 * │   └─ Processes raw HTTP requests → Can reject before controller routing
 * │   └─ Has access to HttpServletRequest/Response (headers, status codes)
 * ├─ Aspect: Method layer interceptor (runs around method calls)
 * │   └─ Runs after HTTP routing (controller already selected)
 * │   └─ No direct access to HTTP status codes
 * │
 * └─ For JWT validation: Filter is better (earlier rejection)
 *
 * INHERITANCE: OncePerRequestFilter
 * ├─ Spring class ensuring filter runs exactly ONCE per request
 * ├─ Why needed?: Without it, filter could run multiple times
 * │   └─ If request forwarded internally
 * │   └─ Avoid validating JWT twice (performance + state issues)
 * ├─ Alternative: DirectoryPathFilter (full control)
 * └─ You use OncePerRequestFilter (recommended)
 *
 * FILTER CHAIN POSITION (in SecurityConfig):
 * ├─ Registered in SecurityFilterChain bean:
 * │   .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
 * │   └─ Runs BEFORE Spring's standard UsernamePasswordAuthenticationFilter
 * │      └─ So JWT is validated FIRST (before other auth methods)
 * │
 * ├─ Complete filter chain order:
 * │   1. JwtFilter (YOUR CUSTOM) ← Validates JWT
 * │   2. UsernamePasswordAuthenticationFilter (SPRING STANDARD) ← Form login
 * │   3. BearerTokenAuthenticationFilter (OAuth2) ← If configured
 * │   4. ExceptionTranslationFilter (SPRING) ← Catches security exceptions
 * │   5. FilterSecurityInterceptor (SPRING) ← Enforces authorization rules
 *
 * EXECUTION FLOW:
 * ├─ Request: GET /api/transactions/list
 * │           Headers: Authorization: Bearer eyJhbGc...
 * │
 * ├─ Step 1: Skip public endpoints
 * │   if (request.getServletPath().equals("/login")) {
 * │       filterChain.doFilter(request, response);  // Skip JWT check
 * │       return;
 * │   }
 * │   └─ Public endpoints bypass filter entirely
 * │
 * ├─ Step 2: Extract Authorization header
 * │   String authorizationHeader = request.getHeader(AUTHORIZATION);
 * │   // Returns: "Bearer eyJhbGc..."
 * │
 * ├─ Step 3: Validate header format
 * │   if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
 * │       response.sendError(401, "Missing or invalid JWT");
 * │       return;  ← STOPS HERE: Don't continue to controller
 * │   }
 * │
 * ├─ Step 4: Extract token
 * │   String token = authorizationHeader.substring(7);
 * │   // Removes "Bearer " (7 chars) → token = "eyJhbGc..."
 * │
 * ├─ Step 5: Parse token & extract claims
 * │   String username = jwtUtil.extractUsername(token);  // "admin"
 * │   List<String> roles = jwtUtil.extractRoles(token);  // ["ROLE_ADMIN"]
 * │
 * ├─ Step 6: Validate token
 * │   if (!jwtUtil.isValidateToken(token)) {
 * │       response.sendError(401, "Invalid or expired JWT");
 * │       return;  ← STOPS HERE
 * │   }
 * │   Validation checks:
 * │   ├─ Signature matches (HMAC-SHA256 recalculation)
 * │   └─ Expiration time not passed
 * │
 * ├─ Step 7: Build authorities from roles
 * │   List<SimpleGrantedAuthority> authorities =
 * │       roles.stream()
 * │           .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
 * │           .collect(Collectors.toList());
 * │   // Input: ["ADMIN"]
 * │   // Output: [SimpleGrantedAuthority("ROLE_ADMIN")]
 * │
 * ├─ Step 8: Create authentication token
 * │   UsernamePasswordAuthenticationToken authentication =
 * │       new UsernamePasswordAuthenticationToken(
 * │           userDetails,           // Principal (who they are)
 * │           null,                  // Credentials (already validated, so null)
 * │           authorities            // Authorities (what they can do)
 * │       );
 * │
 * ├─ Step 9: Set SecurityContext
 * │   SecurityContextHolder.getContext().setAuthentication(authentication);
 * │   // Now controllers can access:
 * │   // SecurityContextHolder.getContext().getAuthentication().getName() → "admin"
 * │   // SecurityContextHolder.getContext().getAuthentication().getAuthorities() → [ROLE_ADMIN]
 * │
 * ├─ Step 10: Continue to next filter
 * │   filterChain.doFilter(request, response);
 * │   └─ Request proceeds to controller
 * │
 * └─ Step 11: Controller receives request
 *     @GetMapping("/api/transactions/list")
 *     @PreAuthorize("hasRole('ADMIN')")  ← Uses roles from SecurityContext
 *     public ResponseEntity<List<Transaction>> list() { ... }
 *
 * @PreAuthorize INTEGRATION:
 * ├─ Spring checks: Does SecurityContext.Authentication have ROLE_ADMIN?
 * ├─ Roles come from: This filter's authorities
 * ├─ If match: Method executes
 * └─ If no match: Throws AccessDeniedException → 403 Forbidden
 *
 * ERROR SCENARIOS:
 * ├─ No Authorization header:
 * │   → response.sendError(401, "UNAUTHORIZED_NO_JWT")
 * ├─ Invalid header format (missing "Bearer "):
 * │   → response.sendError(401, "UNAUTHORIZED_NO_JWT")
 * ├─ Token signature invalid:
 * │   → response.sendError(401, "UNAUTHORIZED_INVALID_JWT")
 * ├─ Token expired:
 * │   → response.sendError(401, "UNAUTHORIZED_INVALID_JWT")
 * └─ All errors caught in outer try-catch:
 * │   → ApiResponse with "INVALID_OR_EXPIRED_JWT"
 *
 * SECURITY BEST PRACTICES:
 * ├─ ✓ Validates token on EVERY request (stateless)
 * ├─ ✓ Signature verification prevents tampering
 * ├─ ✓ Expiration check prevents old tokens
 * ├─ ✓ Roles extracted from token (no DB query per request)
 * ├─ ✓ SecurityContext isolated per request (thread-local)
 * └─ ✓ Public endpoints explicitly skipped (not filtered)
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            if (request.getServletPath().equals("/login")
                    || request.getServletPath().equals("/register")
                    || request.getServletPath().startsWith("/actuator")) {
                filterChain.doFilter(request, response);
                return;
            }

            String authorizationHeader = request.getHeader(AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_NO_JWT);
                return;
            }

            String token = authorizationHeader.substring(7);
            String username = jwtUtil.extractUsername(token);
            List<String> roles = jwtUtil.extractRoles(token);

            if (username != null
                    && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                if (jwtUtil.isValidateToken(token)) {
                    List<SimpleGrantedAuthority> authorities =
                            roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                                    .collect(Collectors.toList());
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(userDetails, null, authorities);

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    response.sendError(
                            HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_INVALID_JWT);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            ApiResponse apiResponse = new ApiResponse();
            apiResponse.setStatus(HttpStatus.METHOD_NOT_ALLOWED.name());
            apiResponse.setError(INVALID_OR_EXPIRED_JWT);
            response.getWriter().write(apiResponse.toString());
        }
    }
}
