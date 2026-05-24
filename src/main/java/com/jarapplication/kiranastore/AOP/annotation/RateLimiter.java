package com.jarapplication.kiranastore.AOP.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ANNOTATION: @RateLimiter - Per-Method Rate Limiting Configuration
 *
 * WHAT IT DOES:
 * ├─ Configurable annotation (contains parameter)
 * ├─ Specifies max requests allowed per minute
 * └─ Tells RateLimiterAspect how many tokens this method gets
 *
 * HOW TO USE:
 * ├─ Example 1: Default 10 requests/minute
 * │   @RateLimiter  ← Uses limit() default value
 * │   @PostMapping(\"/api/auth/login\")
 * │   public AuthResponse login(LoginRequest req) { ... }
 * │
 * ├─ Example 2: Custom limit 5 requests/minute
 * │   @RateLimiter(limit=5)  ← Override default
 * │   @PostMapping(\"/api/auth/register\")
 * │   public void register(UserRequest req) { ... }
 * │   └─ More restrictive: Important endpoint
 * │
 * └─ Example 3: High limit for public endpoint
 *    @RateLimiter(limit=100)
 *    @GetMapping(\"/api/products\")
 *    public List<Product> getProducts() { ... }
 *
 * ANNOTATION PARAMETERS:
 * ├─ limit (int):
 * │   ├─ Meaning: Max tokens available per minute
 * │   ├─ Range: Typically 1-1000
 * │   ├─ Default: 10 (if not specified)
 * │   └─ Default keyword:
 * │       int limit() default 10;
 * │       └─ If @RateLimiter (no value) → limit=10
 * │       └─ If @RateLimiter(limit=5) → limit=5
 * │
 * └─ Why default?
 *    ├─ Reduces boilerplate: Most methods probably need similar limit
 *    ├─ @RateLimiter is shorter than @RateLimiter(limit=10)
 *    └─ Can override when needed: @RateLimiter(limit=5)
 *
 * @Target(ElementType.METHOD):
 * ├─ Can ONLY be placed on method declarations
 * ├─ ✓ Valid: @RateLimiter public void process() {}
 * ├─ ✓ Valid: @RateLimiter(limit=5) public void save() {}
 * └─ ✗ Invalid: @RateLimiter private String username; (field, not method)
 *    └─ Compiler error if attempted
 *
 * @Retention(RetentionPolicy.RUNTIME):
 * ├─ Annotation available at runtime via reflection
 * ├─ RateLimiterAspect needs to READ this at runtime:
 * │   Method method = signature.getMethod();
 * │   RateLimiter limiter = method.getAnnotation(RateLimiter.class);
 * │   int limit = limiter.limit();  // Reads the value
 * │
 * └─ If RETENTION was CLASS or SOURCE: annotation erased → aspect can't read
 *
 * RUNTIME PROCESSING (How RateLimiterAspect reads this):
 * ├─ Step 1: Intercept method call
 * ├─ Step 2: Get method reflection object
 * │   MethodSignature signature = (MethodSignature) joinPoint.getSignature();
 * │   Method method = signature.getMethod();
 * │
 * ├─ Step 3: Read annotation from method
 * │   RateLimiter rateLimiter = method.getAnnotation(RateLimiter.class);
 * │
 * ├─ Step 4: Extract parameter value
 * │   int limit = rateLimiter.limit();  // Returns 10, 5, 100, etc.
 * │
 * └─ Step 5: Create bucket with limit
 *    Bandwidth.classic(limit, Refill.greedy(limit, Duration.ofMinutes(1)))
 *
 * ANNOTATION MEMBER SYNTAX:
 * ├─ Method pattern: int limit() default 10;
 * │   └─ NOT: int limit = 10; (incorrect syntax)
 * │   └─ Annotations use method-like declarations
 * │
 * ├─ Can have multiple members:
 * │   public @interface RateLimiter {
 * │       int limit() default 10;
 * │       int timeWindowMinutes() default 1;
 * │       String errorMessage() default \"Rate limit exceeded\";
 * │   }
 * │
 * └─ Usage:
 *    @RateLimiter(limit=5, timeWindowMinutes=1, errorMessage=\"Too fast\")
 *    public void process() { ... }
 *
 * DIFFERENCE: @RateLimiter vs RateLimiterFilter
 * ├─ @RateLimiter (this annotation):
 * │   ├─ Per-method basis
 * │   ├─ Different limits for different endpoints
 * │   ├─ Examples:
 * │   │   ├─ @RateLimiter(limit=5) login
 * │   │   ├─ @RateLimiter(limit=10) register
 * │   │   └─ @RateLimiter(limit=100) getProducts
 * │   └─ More granular control
 * │
 * └─ RateLimiterFilter:
 *    ├─ Global basis (all endpoints)
 *    ├─ Single limit: 100 requests/min for entire app
 *    └─ Less flexible
 *
 * BEST PRACTICES:
 * ├─ ✓ Use on expensive operations: login, register, transactions
 * ├─ ✓ Set lower limits for sensitive endpoints
 * ├─ ✓ Set higher limits for read-only operations
 * ├─ ✓ Document the limit chosen: \"5 due to external API calls\"
 * ├─ ✗ DON'T set limit=0 or negative (causes issues)
 * ├─ ✗ DON'T place on private methods (won't work via proxy)
 * └─ ✗ DON'T use extremely high values (defeats purpose)
 *
 * EXAMPLE RATE LIMIT STRATEGY:
 * ├─ Public endpoints: @RateLimiter(limit=100)
 * ├─ User endpoints: @RateLimiter(limit=50)
 * ├─ Auth endpoints: @RateLimiter(limit=10)
 * └─ Admin endpoints: @RateLimiter(limit=5)
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimiter {
    int limit() default 10;
}
