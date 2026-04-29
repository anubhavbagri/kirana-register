package com.jarapplication.kiranastore.AOP.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** CapitalizeMethod to check for particular methods to Capitalize fields */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CapitalizeMethod {}
