package com.jarapplication.kiranastore.AOP;

import com.jarapplication.kiranastore.AOP.annotation.RateLimiter;
import com.jarapplication.kiranastore.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

/**
 * RateLimiterAspect: Per-Method Rate Limiting via Token Bucket Algorithm
 *
 * WHAT IT DOES:
 * ├─ Intercepts methods annotated with @RateLimiter(limit=X)
 * ├─ Creates per-method token bucket (X tokens per 1 minute)
 * ├─ Consumes 1 token per request
 * ├─ If tokens available: allows method execution
 * └─ If no tokens: throws RateLimitExceededException (429 Too Many Requests)
 *
 * WHY IT'S NEEDED:
 * ├─ DoS protection: Prevents clients from overwhelming expensive endpoints
 * ├─ Fair usage: Ensures resource sharing among users
 * ├─ Granular control: Different limits for different methods
 * ├─ Declarative: Use annotation, no manual rate-limit checks in code
 * └─ Automatic recovery: Tokens refill every minute (greedy refill)
 *
 * DIFFERS FROM RateLimiterFilter:
 * ├─ Filter: Global rate limit (100 req/min, all endpoints)
 * ├─ Aspect: Per-method limits (granular, different per endpoint)
 * ├─ EXAMPLE:
 * │  ├─ POST /api/auth/login: @RateLimiter(limit=10)  ← 10 req/min
 * │  └─ GET /api/products: @RateLimiter(limit=100)    ← 100 req/min
 *
 * TOKEN BUCKET ALGORITHM (io.github.bucket4j):
 * ├─ Bucket: Container with N tokens
 * ├─ Refill: Strategy to add tokens back after consumption
 * ├─ Bandwidth: Max capacity + refill rate
 * │   └─ Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)))
 * │      ├─ 100 tokens max capacity
 * │      ├─ Greedy: instantly add 100 tokens (not slowly)
 * │      └─ Every 1 minute (refill interval)
 * │
 * ├─ Example timeline:
 * │  ├─ 10:00:00 → Bucket has 100 tokens
 * │  ├─ 10:00:05 → 95 requests made → 5 tokens left
 * │  ├─ 10:00:50 → 20 more requests attempted → 0 tokens left → BLOCKED
 * │  ├─ 10:01:00 → Bucket REFILLS instantly to 100 tokens
 * │  └─ 10:01:01 → Requests proceed again
 *
 * WHY ConcurrentHashMap (Thread Safety):
 * ├─ Multiple threads can call @RateLimiter methods simultaneously
 * ├─ ConcurrentHashMap.putIfAbsent(): Atomic check-and-insert
 * ├─ No synchronization blocks (faster than synchronized Map)
 * ├─ Each thread safely creates/accesses method bucket
 * └─ Alternative: Using Single global bucket for all users (trade-off)
 *
 * USAGE EXAMPLE:
 * ├─ In controller or service:
 * │   @RateLimiter(limit=5)  ← Max 5 requests per minute
 * │   @PostMapping("/expensive-operation")
 * │   public Response process() { ... }
 * │
 * ├─ Requests 1-5: Success (200 OK)
 * ├─ Request 6: Blocked → RateLimitExceededException → 429 Too Many Requests
 * ├─ Wait until next minute refill
 * └─ Request 7 (after refill): Success
 *
 * COMPARISON WITH ALTERNATIVES:
 * ├─ Manual rate limiting:
 * │   ├─ Write logic in every endpoint → Boilerplate
 * │   ├─ Inconsistent implementation → Maintenance nightmare
 * │   └─ Hard to test
 * │
 * ├─ Aspect-based (your approach):
 * │   ├─ Single annotation → Clean code
 * │   ├─ Centralized logic (here) → Easy to modify
 * │   └─ Easy to test: Mock aspect or remove annotation
 * │
 * └─ API Gateway rate limiting:
 * │   ├─ Nginx/Kong handles globally
 * │   ├─ But can't differentiate per-app logic
 * │   └─ Complementary to this approach (use both!)
 *
 * GOTCHAS:
 * ├─ Per-METHOD bucket: Each method has independent bucket
 * ├─ NOT per-user: All users share same bucket
 * ├─ To limit per-user: Extract userId and create separate bucket key
 * ├─ Token refill: Greedy = instant (not incremental)
 * └─ Memory: Each method keeps bucket in memory (HashMap)
 *    └─ If 100 methods with @RateLimiter → 100 bucket objects in memory
 */
@Aspect
@Component
public class RateLimiterAspect {

    private final Map<Method, Bucket> rateLimiterHashMap = new ConcurrentHashMap<>();

    /**
     * Executes the function when annotated
     *
     * @param joinPoint
     * @throws IllegalAccessException
     */
    @Around("@annotation(com.jarapplication.kiranastore.AOP.annotation.RateLimiter)")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
        int limit = rateLimiter.limit();
        rateLimiterHashMap.putIfAbsent(
                method,
                Bucket.builder()
                        .addLimit(
                                Bandwidth.classic(
                                        limit, Refill.greedy(limit, Duration.ofMinutes(1))))
                        .build());
        Bucket bucket = rateLimiterHashMap.get(method);
        if (bucket.tryConsume(1)) {
            return joinPoint.proceed(); // Proceed with the method execution
        } else {
            throw new RateLimitExceededException("Too many requests. Please try again later.");
        }
    }
}
