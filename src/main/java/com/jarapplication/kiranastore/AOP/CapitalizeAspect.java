package com.jarapplication.kiranastore.AOP;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * CapitalizeAspect: AOP Aspect for Auto-Uppercasing String Fields
 *
 * WHAT IT DOES:
 * ├─ Intercepts methods annotated with @CapitalizeMethod
 * ├─ Scans all fields in method parameters for @Capitalize annotation
 * ├─ Automatically converts String values to UPPERCASE before method execution
 * └─ Executes original method with mutated data
 *
 * WHY IT'S NEEDED:
 * ├─ Cross-cutting concern: Many DTOs need normalized uppercase fields
 * ├─ Eliminates boilerplate: No need to call .toUpperCase() in every service
 * ├─ Consistency: Ensures uniform data transformation across codebase
 * └─ Maintainability: Change uppercase logic in ONE place (here)
 *
 * HOW IT WORKS (REFLECTION + INTERCEPTOR PATTERN):
 * ├─ @Aspect: Tells Spring this is an AOP aspect (intercepts method calls)
 * ├─ @Component: Registers as Spring-managed bean (auto-wired at startup)
 * └─ @Around: Interception type = execute code BEFORE and AFTER method
 *
 * EXAMPLE USAGE:
 * ├─ In UserServiceImp.java:
 * │   @Override
 * │   @CapitalizeMethod  ← This triggers CapitalizeAspect.capitalizeFields()
 * │   public UserRequest save(UserRequest userRequest) { ... }
 * │
 * ├─ Input: UserRequest(username="john")
 * ├─ Aspect intercepts: Finds @Capitalize on username field
 * ├─ Mutates: username="john" → "JOHN"
 * └─ Output: save(UserRequest(username="JOHN")) → method continues with uppercase data
 *
 * REFLECTION DETAILS (java.lang.reflect.Field):
 * ├─ entity.getClass().getDeclaredFields(): Gets all declared fields (private, protected, public)
 * ├─ field.setAccessible(true): Bypasses private/protected visibility for reflection
 * ├─ field.get(entity): Reads current value from field
 * ├─ field.set(entity, value): Writes new value to field
 * └─ null check: Prevents NPE on null strings
 *
 * PERFORMANCE CONSIDERATIONS:
 * ├─ Reflection is slower than direct method calls (~10-100x slower)
 * ├─ Called for EVERY method with @CapitalizeMethod (potential bottleneck)
 * ├─ Consider caching if used on high-traffic endpoints
 * └─ Trade-off: Code cleanliness vs. performance (acceptable for business logic)
 *
 * RELATED PATTERNS:
 * ├─ @Around: AOP interceptor (before + after execution)
 * ├─ @Before: Only before method execution
 * ├─ @After: Only after method execution
 * ├─ @AfterReturning: Only if method returns successfully
 * ├─ @AfterThrowing: Only if method throws exception
 * └─ See RateLimiterAspect.java for @Around with error throwing
 */
@Aspect
@Component
public class CapitalizeAspect {

    /**
     * Executes the function when annotated
     *
     * @param joinPoint
     * @throws IllegalAccessException
     */
    @Around("@annotation(com.jarapplication.kiranastore.AOP.annotation.CapitalizeMethod)")
    public Object capitalizeFields(ProceedingJoinPoint joinPoint) throws Throwable {
        Object entity = joinPoint.getArgs()[0];

        if (entity != null) {
            for (Field field : entity.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Capitalize.class)
                        && field.getType().equals(String.class)) {
                    field.setAccessible(true);
                    String value = (String) field.get(entity);
                    if (value != null) {
                        field.set(entity, value.toUpperCase());
                    }
                }
            }
        }
        return joinPoint.proceed();
    }
}
