package com.delphi.delphi.dtos;

import java.time.LocalDateTime;

import com.delphi.delphi.utils.exceptions.AuthenticationException.ErrorType;

public class AuthErrorResponseDto {
    private String error;
    private String message;
    private ErrorType errorType;
    private LocalDateTime timestamp;
    private String path;
    
    public AuthErrorResponseDto() {
        this.timestamp = LocalDateTime.now();
    }
    
    public AuthErrorResponseDto(String error, String message, ErrorType errorType, String path) {
        this();
        this.error = error;
        this.message = message;
        this.errorType = errorType;
        this.path = path;
    }
    
    public AuthErrorResponseDto(String error, String message, ErrorType errorType) {
        this(error, message, errorType, null);
    }
    
    // Getters and Setters
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public ErrorType getErrorType() {
        return errorType;
    }
    
    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
} 