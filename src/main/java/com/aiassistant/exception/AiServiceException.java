package com.aiassistant.exception;

import lombok.Getter;

/**
 * Base exception for AI service operations.
 * Follows Exception hierarchy principles.
 */
@Getter
public class AiServiceException extends RuntimeException {
    
    private final String errorCode;
    private final Object[] args;
    
    public AiServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.args = new Object[0];
    }
    
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.args = new Object[0];
    }
    
    public AiServiceException(String errorCode, String message, Object... args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
    }
    
    public AiServiceException(String errorCode, String message, Throwable cause, Object... args) {
        super(message, cause);
        this.errorCode = errorCode;
        this.args = args;
    }
}
