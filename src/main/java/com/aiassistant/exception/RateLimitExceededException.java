package com.aiassistant.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * Extends AiServiceException for consistent error handling.
 */
public class RateLimitExceededException extends AiServiceException {
    
    public RateLimitExceededException(String message) {
        super("RATE_LIMIT_EXCEEDED", message);
    }
    
    public RateLimitExceededException(String message, int limit, int timeWindowMinutes) {
        super("RATE_LIMIT_EXCEEDED", message, limit, timeWindowMinutes);
    }
}
