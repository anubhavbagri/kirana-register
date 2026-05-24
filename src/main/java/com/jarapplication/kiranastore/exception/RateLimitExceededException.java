package com.jarapplication.kiranastore.exception;

/**
 * CUSTOM EXCEPTION: RateLimitExceededException
 *
 * WHAT IT DOES:
 * ├─ Thrown when request count exceeds allowed limit (429 Too Many Requests)
 * ├─ Thrown by RateLimiterAspect when token bucket is empty
 * ├─ Caught by ExceptionController.java → Returns rate-limit error response
 * └─ Signals client to back off and retry later
 *
 * WHY THIS EXCEPTION:
 * ├─ Resource protection: Prevent client from hammering expensive endpoints
 * ├─ Clear error message: Client knows it's rate-limit (not permission denied)
 * └─ Retryable: Client can implement exponential backoff
 *    └─ Other exceptions (UserNameExists) are NOT retryable
 *       └─ Rate limit IS retryable: wait then retry
 *
 * THROWN BY (RateLimiterAspect.java):
 * ├─ Location: When bucket.tryConsume(1) returns false
 * │   └─ throw new RateLimitExceededException("Too many requests...")
 * │
 * └─ Flow:
 *    1. Client makes request to @RateLimiter endpoint
 *    2. RateLimiterAspect intercepts
 *    3. Check token bucket
 *    4. If NO tokens → throw this exception
 *    5. ExceptionController catches it
 *    6. Returns error response with status="429"
 *
 * CLIENT-SIDE HANDLING:
 * ├─ Detect error code "429"
 * ├─ Implement exponential backoff:
 * │   ├─ 1st retry: Wait 1 second
 * │   ├─ 2nd retry: Wait 2 seconds
 * │   ├─ 3rd retry: Wait 4 seconds
 * │   └─ Continue until success or max retries
 * │
 * └─ HTTP Header: Retry-After
 *    └─ Server can suggest wait time
 *       └─ "Retry-After: 60"  ← Wait 60 seconds
 *
 * DIFFERENCE FROM OTHER EXCEPTIONS:
 * ├─ UserNameExistsException: NOT retryable → Different username
 * ├─ IllegalArgumentException: NOT retryable → Fix your input
 * └─ RateLimitExceededException: RETRYABLE → Just wait and try again
 *
 * BEST PRACTICES:
 * ├─ ✓ Throw when rate limit hit
 * ├─ ✓ Include friendly message
 * ├─ ✓ ExceptionController returns consistent error format
 * ├─ ✓ Client recognizes 429 as rate-limit (standard HTTP code)
 * └─ ✓ Implement backoff strategy client-side
 *
 * REAL-WORLD EXAMPLE:
 * ├─ Endpoint: POST /api/auth/login (high-cost operation)
 * ├─ Rate limit: 10 requests per minute
 * ├─ Client attempts: 15 rapid requests
 * ├─ Requests 1-10: Success (tokens consumed)
 * ├─ Requests 11-15: RateLimitExceededException thrown
 * └─ Response to client:
 *    {
 *        "success": false,
 *        "status": "429",
 *        "errorMessage": "Too many requests. Please try again later."
 *    }
 *
 *    └─ Client logic:
 *       if (response.status === "429") {
 *           setTimeout(() => retry(), 60000);  // Wait 1 minute
 *       }
 */
public class RateLimitExceededException extends RuntimeException {
    public RateLimitExceededException(String message) {
        super(message);
    }
}
