package com.delphi.delphi.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.delphi.delphi.dtos.AuthErrorResponseDto;
import com.delphi.delphi.utils.exceptions.AuthenticationException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthErrorResponseDto> handleAuthenticationException(
            com.delphi.delphi.utils.exceptions.AuthenticationException ex, 
            HttpServletRequest request) {
        
        log.warn("Authentication exception: {} for path: {}", ex.getMessage(), request.getRequestURI());
        
        AuthErrorResponseDto errorResponse = new AuthErrorResponseDto(
            "Authentication Failed",
            ex.getMessage(),
            ex.getErrorType(),
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<AuthErrorResponseDto> handleBadCredentialsException(
            BadCredentialsException ex, 
            HttpServletRequest request) {
        
        log.warn("Bad credentials exception for path: {} - Root cause: {}", request.getRequestURI(), ex.getCause());
        
        // Check if the root cause is UsernameNotFoundException
        if (ex.getCause() instanceof org.springframework.security.core.userdetails.UsernameNotFoundException) {
            log.warn("Username not found exception for path: {}", request.getRequestURI());
            
            AuthErrorResponseDto errorResponse = new AuthErrorResponseDto(
                "Authentication Failed",
                "No account found with this email address",
                com.delphi.delphi.utils.exceptions.AuthenticationException.ErrorType.USER_NOT_FOUND,
                request.getRequestURI()
            );
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
        
        // Otherwise, it's an invalid password
        AuthErrorResponseDto errorResponse = new AuthErrorResponseDto(
            "Authentication Failed",
            "Invalid email or password",
            com.delphi.delphi.utils.exceptions.AuthenticationException.ErrorType.INVALID_PASSWORD,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<AuthErrorResponseDto> handleSpringAuthenticationException(
            org.springframework.security.core.AuthenticationException ex, 
            HttpServletRequest request) {
        
        log.warn("Spring authentication exception: {} for path: {}", ex.getMessage(), request.getRequestURI());
        
        AuthErrorResponseDto errorResponse = new AuthErrorResponseDto(
            "Authentication Failed",
            "Authentication failed. Please check your credentials.",
            com.delphi.delphi.utils.exceptions.AuthenticationException.ErrorType.UNKNOWN_ERROR,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AuthErrorResponseDto> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        log.error("Unexpected error for path: {}", request.getRequestURI(), ex);
        
        AuthErrorResponseDto errorResponse = new AuthErrorResponseDto(
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            com.delphi.delphi.utils.exceptions.AuthenticationException.ErrorType.UNKNOWN_ERROR,
            request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
} 