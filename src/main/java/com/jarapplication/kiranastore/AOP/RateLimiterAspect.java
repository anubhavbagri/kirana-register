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
