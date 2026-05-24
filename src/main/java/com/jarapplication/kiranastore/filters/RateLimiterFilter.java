package com.jarapplication.kiranastore.filters;

import static com.jarapplication.kiranastore.constants.LogConstants.TOO_MANY_REQUESTS;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * RATE LIMITER FILTER: Global HTTP-Level Rate Limiting (100 req/min)
 *
 * WHAT IT DOES:
 * ├─ Runs on EVERY HTTP request (before any controller)
 * ├─ Limits total requests to 100 per minute across the entire application
 * ├─ Uses Bucket4j token bucket algorithm (same library as RateLimiterAspect)
 * └─ Returns HTTP 429 (Too Many Requests) when limit exceeded
 *
 * WHY IT'S NEEDED:
 * ├─ Global protection: Rate limits ALL endpoints (not just @RateLimiter annotated ones)
 * ├─ Different from @RateLimiter AOP:
 * │   ├─ This filter: GLOBAL, 100 req/min, HTTP-level, all endpoints
 * │   └─ @RateLimiter AOP: PER-METHOD, configurable limit, method-level, annotated only
 * ├─ Defense in depth: Even if AOP is bypassed, this filter catches excessive traffic
 * └─ Infrastructure-level protection (filters run before Spring dispatch)
 *
 * TOKEN BUCKET (Same Algorithm as RateLimiterAspect):
 * ├─ Capacity: 100 tokens
 * ├─ Refill: 100 tokens every 1 minute (greedy = all at once)
 * ├─ Each request: Consumes 1 token
 * ├─ Tokens available → request proceeds
 * └─ No tokens → HTTP 429 response (request blocked)
 *
 * IMPORTANT LIMITATIONS:
 * ├─ SINGLE BUCKET for entire application (not per-user or per-IP)
 * │   └─ 1 heavy user can exhaust tokens for ALL users
 * │   └─ Improvement: Use ConcurrentHashMap<IP, Bucket> for per-IP limiting
 * ├─ Shared across all endpoints (including /login, /register)
 * │   └─ Actuator endpoints are excluded (if path starts with /actuators/**)
 * └─ In-memory: Resets on server restart (no Redis persistence)
 *
 * FILTER vs AOP:
 * ├─ Filter (THIS): Runs in HTTP pipeline BEFORE Spring dispatches to controller
 * │   └─ Can reject request before any Spring beans are involved
 * │   └─ Has access to raw HTTP request/response
 * ├─ AOP (@RateLimiter): Runs AROUND specific annotated methods
 * │   └─ More granular (per-method limits)
 * │   └─ Requires Spring proxy (method must be called externally)
 * └─ Both complement each other: Filter = global cap, AOP = fine-grained limits
 *
 * NOTE: Path check uses .startsWith("/actuators/**") which includes the glob pattern
 *       as literal text. The "**" is NOT a wildcard in String.startsWith().
 *       Should be: .startsWith("/actuator") (without glob).
 *
 * @Component: Registers as Spring bean → auto-registered in filter chain
 */
@Component // ← Spring auto-registers this as an HTTP filter
public class RateLimiterFilter extends OncePerRequestFilter {

    // Single token bucket for ALL requests (application-wide rate limit)
    Bucket bucket = this.createNewBucket();

    /**
     * Creates a new token bucket: 100 tokens, refilled every minute.
     *
     * Bandwidth.classic(capacity, refill):
     * ├─ capacity=100: Maximum tokens in bucket
     * ├─ Refill.greedy(100, 1 minute): Refill all 100 tokens at once every minute
     * └─ greedy vs intervally:
     *    ├─ greedy: All tokens added at minute boundary
     *    └─ intervally: Tokens added evenly throughout the minute
     *
     * @return Bucket with 100 tokens per minute rate limit
     */
    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1))))
                .build();
    }

    /**
     * Filters every HTTP request for rate limiting.
     *
     * FLOW:
     * ├─ 1. Skip actuator endpoints (monitoring/health)
     * ├─ 2. Try to consume 1 token from bucket
     * ├─ 3. If token consumed → proceed to next filter (JwtFilter → controller)
     * └─ 4. If no tokens → HTTP 429 response → request ends here
     *
     * @param request     ← HTTP request
     * @param response    ← HTTP response (write 429 if rate limited)
     * @param filterChain ← Continue to next filter if allowed
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip actuator endpoints (health checks, metrics should not be rate limited)
        // NOTE: "/actuators/**" includes glob pattern literally — should be "/actuator"
        if (request.getServletPath().startsWith("/actuators/**")) {
            filterChain.doFilter(request, response);
            return;
        }
        // Try to consume 1 token from the bucket
        if (!bucket.tryConsume(1)) {
            // No tokens available → reject with HTTP 429
            response.setStatus(429); // ← 429 Too Many Requests
            response.getWriter().write(TOO_MANY_REQUESTS); // ← "Too Many Requests. "
            return; // ← Request ends here (no controller invoked)
        }

        // Token consumed → allow request to proceed
        filterChain.doFilter(request, response);
    }
}
