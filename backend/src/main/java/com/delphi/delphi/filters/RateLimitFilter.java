package com.delphi.delphi.filters;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.RedisService;
import com.delphi.delphi.services.JwtService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(4)
public class RateLimitFilter implements Filter {

    private static final int RATE_LIMIT_PER_MINUTE = 20;
    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";

    private final RedisService redisService;
    private final JwtService jwtService;

    public RateLimitFilter(RedisService redisService, JwtService jwtService) {
        this.redisService = redisService;
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No auth header, skip rate limiting (or apply different rules)
            chain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        
        // Validate JWT first
        if (!jwtService.validateToken(jwt)) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        String userEmail = jwtService.extractEmail(jwt);
        
        // Implement the rate limiting algorithm from pseudocode
        if (!isRequestAllowed(userEmail)) {
            // Line 3: Show error message and end connection
            res.setStatus(429); // HTTP 429 Too Many Requests
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum " + RATE_LIMIT_PER_MINUTE + " requests per minute.\"}");
            return;
        }

        // Line 5: Do service stuff (continue to next filter)
        chain.doFilter(request, response);
    }

    private boolean isRequestAllowed(String userEmail) {
        // Get current minute number (timestamp divided by 60 seconds)
        long currentMinute = System.currentTimeMillis() / (60 * 1000);
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + userEmail + ":" + currentMinute;

        // Line 1: GET [user-api-key]:[current minute number]
        Long currentCount = redisService.getLong(rateLimitKey);

        // Line 2: If the result is less than 20 (or unset) go to 4, otherwise line 3
        if (currentCount >= RATE_LIMIT_PER_MINUTE) {
            return false; // Rate limit exceeded
        }

        // Line 4: MULTI/INCR/EXPIRE/EXEC transaction (atomic operation)
        try {
            // This implements the exact pseudocode: INCR and EXPIRE in a single transaction
            Long newCount = redisService.incrementAndExpire(rateLimitKey, 59);
            
            // Check if the new count exceeds the limit
            return newCount <= RATE_LIMIT_PER_MINUTE;
        } catch (Exception e) {
            // In case of Redis error, allow the request (fail open)
            return true;
        }
    }
}
