package com.jarapplication.kiranastore.exception;

/** Rate limit exceeded Exception */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
