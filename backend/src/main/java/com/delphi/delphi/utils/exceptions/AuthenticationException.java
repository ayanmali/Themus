package com.delphi.delphi.utils.exceptions;

public class AuthenticationException extends RuntimeException {
    
    public enum ErrorType {
        USER_NOT_FOUND("User not found"),
        INVALID_PASSWORD("Invalid password"),
        ACCOUNT_DISABLED("Account is disabled"),
        ACCOUNT_LOCKED("Account is locked"),
        CREDENTIALS_EXPIRED("Credentials have expired"),
        EMAIL_ALREADY_EXISTS("Email already exists"),
        VALIDATION_ERROR("Validation error"),
        UNKNOWN_ERROR("Authentication failed");
        
        private final String message;
        
        ErrorType(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    private final ErrorType errorType;
    
    public AuthenticationException(ErrorType errorType) {
        super(errorType.getMessage());
        this.errorType = errorType;
    }
    
    public AuthenticationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    public AuthenticationException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
} 