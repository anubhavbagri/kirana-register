package com.jarapplication.kiranastore.AOP.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ANNOTATION: @CapitalizeMethod - Method-Level Marker for Auto-Uppercasing
 *
 * WHAT IT DOES:
 * ├─ Marker annotation (contains no data, just signal)
 * ├─ Placed on methods to trigger CapitalizeAspect
 * └─ Signals: \"Intercept this method and uppercase @Capitalize fields\"
 *
 * HOW TO USE:
 * ├─ Example 1:
 * │   @CapitalizeMethod  ← Add this decorator
 * │   @Override
 * │   public UserRequest save(UserRequest userRequest) {
 * │       // Aspect intercepts BEFORE method runs
 * │       // Finds @Capitalize fields in userRequest
 * │       // Uppercases them
 * │       // Then runs your method with mutated data
 * │   }
 * │
 * └─ Example 2:
 *    @CapitalizeMethod
 *    public List<UserRequest> getUsers() { ... }
 *
 * ANNOTATION ANATOMY:
 * ├─ @Target({ElementType.METHOD}):
 * │   ├─ Restricts where annotation can be placed
 * │   ├─ ElementType.METHOD = only on methods
 * │   ├─ If you try: @CapitalizeMethod class MyClass {} → COMPILATION ERROR
 * │   └─ Alternative targets:
 * │       ├─ ElementType.FIELD = only on fields
 * │       ├─ ElementType.CLASS = only on classes
 * │       ├─ ElementType.PARAMETER = only on parameters
 * │       └─ ElementType.METHOD, ElementType.FIELD = both method & field
 * │
 * └─ @Retention(RetentionPolicy.RUNTIME):
 *    ├─ Specifies when annotation is available
 *    ├─ RetentionPolicy.RUNTIME: Available at runtime (via reflection)
 *    │   └─ Needed for AOP: Aspect reads annotation at runtime
 *    │   └─ Method.getAnnotation(CapitalizeMethod.class) works
 *    ├─ RetentionPolicy.CLASS: Available in bytecode (default)
 *    │   └─ Erased after compilation (reflection won't find it)
 *    └─ RetentionPolicy.SOURCE: Only in source code
 *       └─ Erased after compilation (for documentation)
 *
 * ANNOTATION STRUCTURE:
 * ├─ This annotation has NO MEMBERS (no parameters)
 * │   └─ Compare to @RateLimiter(limit=10) which HAS parameter
 * │
 * ├─ Simple annotations:
 * │   public @interface CapitalizeMethod {}
 * │   └─ Used as @CapitalizeMethod (no parentheses or values)
 * │
 * └─ Annotations with members:
 *    public @interface RateLimiter {
 *        int limit() default 10;
 *    }
 *    └─ Used as @RateLimiter or @RateLimiter(limit=5)
 *
 * JAVA REFLECTION + AOP:
 * ├─ At runtime, CapitalizeAspect does:
 * │   1. Read method signature
 * │   2. Check if method has @CapitalizeMethod annotation
 * │   3. If yes: Apply capitalization logic
 * │   4. If no: Skip (don't intercept)
 * │
 * └─ Code example (in CapitalizeAspect):
 *    @Around(\"@annotation(com.jarapplication.kiranastore.AOP.annotation.CapitalizeMethod)\")
 *    public Object capitalizeFields(ProceedingJoinPoint joinPoint) throws Throwable {
 *        // This method runs if @CapitalizeMethod is present
 *    }
 *
 * COMPARISON: @CapitalizeMethod vs @Capitalize
 * ├─ @CapitalizeMethod (THIS ANNOTATION):
 * │   ├─ @Target(METHOD) = place on methods
 * │   ├─ Signals: \"Intercept this method\"
 * │   └─ Example: @CapitalizeMethod public void save(UserRequest req) {}
 * │
 * └─ @Capitalize (FIELD ANNOTATION):
 *    ├─ @Target(FIELD) = place on fields
 *    ├─ Signals: \"This field should be uppercased\"
 *    └─ Example: @Capitalize private String username;
 *
 * FLOW DIAGRAM:
 * ├─ You write: @CapitalizeMethod public void save(User user) {}
 * ├─ Aspect scans: \"I see @CapitalizeMethod annotation!\"
 * ├─ Aspect intercepts: Aspect.capitalizeFields() method runs
 * ├─ Aspect checks: findAll @Capitalize fields in User object
 * ├─ Aspect mutates: username=\"john\" → \"JOHN\"
 * ├─ Aspect proceeds: Calls actual save() method with uppercase data
 * └─ Result: Database stored uppercase username
 *
 * WHY NOT JUST USE @Capitalize ON FIELD?
 * ├─ @Capitalize on field:
 * │   \u251c\u2500 @Capitalize private String username;
 * │   ├─ Aspect doesn't know which methods to intercept
 * │   ├─ Would have to intercept EVERY method (performance hit)
 * │   └─ Aspect can't distinguish: \"Is this method setting or getting?\"
 * │
 * └─ @CapitalizeMethod on method:
 *    ├─ Explicit: Developer chooses which methods to apply
 *    ├─ Efficient: Only intercepts methods you mark
 *    └─ Clear intent: \"Save should uppercase fields\"
 *       └─ But getUser() should NOT uppercase (return original casing)
 *
 * BEST PRACTICES:
 * ├─ ✓ Place on setter methods (save(), create(), update())
 * ├─ ✓ NOT on getter methods (queries should return original casing)
 * ├─ ✓ Use with @Capitalize on DTO fields
 * ├─ ✓ Document why: \"Ensures consistent uppercase usernames\"
 * ├─ ✗ DON'T place on every method (wastes interception)
 * └─ ✗ DON'T use without matching @Capitalize fields
 *    └─ Annotation runs but finds nothing to capitalize (wasted)
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CapitalizeMethod {}
