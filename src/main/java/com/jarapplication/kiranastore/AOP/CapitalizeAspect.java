package com.jarapplication.kiranastore.AOP;

import com.jarapplication.kiranastore.AOP.annotation.Capitalize;
import java.lang.reflect.Field;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

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
