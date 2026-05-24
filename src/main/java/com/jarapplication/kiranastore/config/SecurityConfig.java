package com.jarapplication.kiranastore.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jarapplication.kiranastore.filters.JwtFilter;
import com.jarapplication.kiranastore.response.ApiResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SECURITY CONFIG: Central Security Configuration for the Kirana Store Application
 *
 * WHAT IT DOES:
 * ├─ Configures Spring Security filter chain (authentication + authorization)
 * ├─ Defines which endpoints are public vs. require authentication
 * ├─ Integrates JWT-based stateless authentication (via JwtFilter)
 * ├─ Sets up password encoding (BCrypt for secure password storage)
 * ├─ Handles access denied (403) and unauthorized (401→403) errors
 * └─ Enables method-level security (@PreAuthorize in controllers)
 *
 * WHY IT'S NEEDED:
 * ├─ Without it: All endpoints are either unprotected or use default Spring Security (form login)
 * ├─ JWT authentication: Stateless, scalable, no server-side session needed
 * ├─ Role-based access: ADMIN-only endpoints (e.g., POST /products/add)
 * └─ Error handling: Consistent JSON error responses for auth failures
 *
 * ANNOTATIONS:
 * ├─ @Configuration: Marks this as a Spring configuration class (contains @Bean methods)
 * │   └─ Processed at startup: Spring creates beans defined here
 * │
 * ├─ @EnableWebSecurity: Activates Spring Security's web security support
 * │   └─ Without it: Spring Security filter chain is not applied
 * │
 * └─ @EnableMethodSecurity(prePostEnabled = true): Enables method-level security
 *    ├─ Allows: @PreAuthorize("hasRole('ADMIN')") on controller methods
 *    ├─ @PreAuthorize: Evaluated BEFORE method execution
 *    ├─ @PostAuthorize: Evaluated AFTER method execution (less common)
 *    └─ Without this: @PreAuthorize annotations are silently ignored!
 *
 * SECURITY FILTER CHAIN (REQUEST PROCESSING ORDER):
 * ├─ 1. HTTP request arrives
 * ├─ 2. JwtFilter (custom, added BEFORE UsernamePasswordAuthenticationFilter):
 * │   ├─ Extracts JWT from Authorization header
 * │   ├─ Validates token (signature, expiration)
 * │   ├─ Sets SecurityContext with authenticated user + roles
 * │   └─ If no/invalid token → request continues WITHOUT authentication
 * │
 * ├─ 3. Authorization check (authorizeHttpRequests):
 * │   ├─ /login, /register, /actuator/** → permitAll() (no auth needed)
 * │   └─ All other requests → authenticated() (must have valid SecurityContext)
 * │       └─ If not authenticated → authenticationEntryPoint (403 + error JSON)
 * │
 * ├─ 4. Method-level security (@PreAuthorize):
 * │   ├─ After request reaches controller
 * │   ├─ Checks user's roles from SecurityContext
 * │   └─ If role doesn't match → accessDeniedHandler (403 + error JSON)
 * │
 * └─ 5. Controller method executes
 *
 * SESSION MANAGEMENT:
 * ├─ SessionCreationPolicy.STATELESS:
 * │   ├─ Spring Security does NOT create HTTP sessions
 * │   ├─ Each request must carry JWT token (self-contained authentication)
 * │   ├─ No session cookies → no CSRF vulnerability → csrf().disable() is safe
 * │   ├─ Scalable: No session storage on server → works with load balancers
 * │   └─ Trade-off: Token can't be revoked (unless using blacklist/Redis)
 * │
 * └─ CSRF Disabled:
 *    ├─ CSRF protection is for session-based auth (form submissions)
 *    ├─ JWT tokens are sent in Authorization header (not cookies)
 *    └─ CSRF attacks can't forge Authorization headers → safe to disable
 *
 * PASSWORD ENCODING (BCrypt):
 * ├─ BCryptPasswordEncoder: Industry-standard password hashing
 * │   ├─ One-way hash: password → hash (cannot reverse)
 * │   ├─ Salt: Random salt added before hashing (prevents rainbow tables)
 * │   ├─ Cost factor: Configurable iterations (default 10, higher = slower)
 * │   └─ Example: "password123" → "$2a$10$dXJ3SW6G7P50lGmMQgel..." (different every time)
 * ├─ Used when: Registering user (hash password before DB save)
 * ├─ Used when: Logging in (hash input → compare with DB hash)
 * └─ NEVER store plain text passwords in database
 *
 * ERROR HANDLERS:
 * ├─ accessDeniedHandler (errorCode "403a"):
 * │   └─ Triggered when: User IS authenticated but doesn't have required role
 * │   └─ Example: Regular user calls POST /products/add (requires ADMIN)
 * │   └─ Response: HTTP 403 + { success: false, errorCode: "403a", errorMessage: "Access Denied" }
 * │
 * └─ authenticationEntryPoint (errorCode "403b"):
 *    └─ Triggered when: User is NOT authenticated (no/invalid JWT)
 *    └─ Example: Anonymous request to GET /v1/api/products (requires auth)
 *    └─ Response: HTTP 403 + { success: false, errorCode: "403b", errorMessage: "User is not authorized" }
 *
 * FILTER ORDER:
 * ├─ .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class):
 * │   └─ JwtFilter runs BEFORE Spring's default username/password filter
 * │   └─ Why? We want JWT auth, not form-based auth
 * │   └─ If JwtFilter sets SecurityContext → Spring skips default filter
 * └─ .formLogin(AbstractHttpConfigurer::disable):
 *    └─ Disables Spring's default login page (/login form)
 *    └─ We use custom /login REST endpoint instead
 */
@Configuration          // ← Spring config class (contains @Bean definitions)
@EnableWebSecurity      // ← Activates Spring Security web filter chain
@EnableMethodSecurity(prePostEnabled = true) // ← Enables @PreAuthorize, @PostAuthorize
public class SecurityConfig {

    /**
     * PASSWORD ENCODER BEAN: BCrypt hashing for secure password storage.
     *
     * Injected into: UserServiceImp (for registration + login)
     * Usage: passwordEncoder.encode("rawPassword") → hashed string
     *        passwordEncoder.matches("rawPassword", hashedPassword) → boolean
     *
     * @return BCryptPasswordEncoder instance (singleton bean)
     */
    @Bean // ← Creates a Spring-managed singleton bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AUTHENTICATION MANAGER BEAN: Spring Security's core auth component.
     *
     * Delegates authentication to configured UserDetailsService + PasswordEncoder.
     * Used in: Login flow (authManager.authenticate(token) → validates credentials)
     *
     * @param authConfig ← Auto-configured by Spring Security
     * @return AuthenticationManager (delegates to UserDetailsService)
     * @throws Exception if configuration fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig)
            throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * SECURITY FILTER CHAIN BEAN: Defines the complete security configuration.
     *
     * This is the core of Spring Security configuration. It defines:
     * ├─ Which URLs are public
     * ├─ How authentication works (JWT)
     * ├─ How authorization failures are handled
     * ├─ Session policy (stateless for JWT)
     * └─ Filter ordering
     *
     * @param http      ← Spring Security's HttpSecurity builder
     * @param jwtFilter ← Custom JWT validation filter (auto-injected)
     * @return Configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtFilter jwtFilter)
            throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // ← Safe: JWT doesn't use cookies (no CSRF risk)
                .authorizeHttpRequests(
                        auth ->
                                auth.requestMatchers("/login", "/register", "/actuator/**")
                                        .permitAll()  // ← Public endpoints: no JWT needed
                                        .anyRequest()
                                        .authenticated()) // ← Everything else: JWT required
                .exceptionHandling(
                        exceptionHandlingConfigurer -> {
                            // ACCESS DENIED: User is authenticated but lacks required role
                            // Example: Regular user → POST /products/add (requires ADMIN)
                            exceptionHandlingConfigurer.accessDeniedHandler(
                                    (request, res, e) -> {
                                        String bearerToken = request.getHeader("Authorization");
                                        ApiResponse apiResponse = new ApiResponse();
                                        apiResponse.setSuccess(false);
                                        apiResponse.setErrorCode("403a"); // ← "a" = access denied
                                        apiResponse.setErrorMessage("Access Denied");
                                        res.setStatus(403);
                                        ObjectMapper mapper = new ObjectMapper();
                                        res.getWriter()
                                                .write(mapper.writeValueAsString(apiResponse));
                                    });
                            // AUTHENTICATION ENTRY POINT: No valid JWT provided
                            // Example: Anonymous request → GET /v1/api/products
                            exceptionHandlingConfigurer.authenticationEntryPoint(
                                    (request, res, e) -> {
                                        ApiResponse apiResponse = new ApiResponse();
                                        apiResponse.setSuccess(false);
                                        apiResponse.setErrorCode("403b"); // ← "b" = not authorized
                                        apiResponse.setErrorMessage("User is not authorized");
                                        ObjectMapper mapper = new ObjectMapper();
                                        res.setStatus(403);
                                        res.getWriter()
                                                .write(mapper.writeValueAsString(apiResponse));
                                    });
                        })
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // JWT runs BEFORE default username/password filter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // Disable Spring's default form login page (we use REST /login endpoint)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
