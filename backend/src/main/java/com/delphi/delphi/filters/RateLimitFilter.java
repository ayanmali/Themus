package com.delphi.delphi.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.delphi.delphi.components.JwtService;
import com.delphi.delphi.components.RedisService;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
@Order(4) // Run after JWT filter and Security filter
// Token bucket rate limiting
public class RateLimitFilter implements Filter {

    private static final int RATE_LIMIT_PER_MINUTE = 20;
    private static final String RATE_LIMIT_KEY_PREFIX = "ratelimit:";

    private final RedisService redisService;
    private final JwtService jwtService;
    private final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    public RateLimitFilter(RedisService redisService, JwtService jwtService) {
        this.redisService = redisService;
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        // Skip rate limiting for permitted endpoints
        String requestPath = req.getRequestURI();
        if (requestPath.startsWith("/api/auth/") || requestPath.startsWith("/api/users/is-authenticated") || requestPath.startsWith("/api/users/github/callback") || requestPath.startsWith("/api/assessments/live") || requestPath.equals("/")) {
            log.info("RateLimitFilter - Skipping rate limiting for permitted endpoint: {}", requestPath);
            chain.doFilter(request, response);
            return;
        }

        // Look for JWT token in cookies first (like JwtAuthFilter does)
        String jwt = null;
        Cookie[] cookies = req.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    log.info("RateLimitFilter - JWT token found in cookies");
                    break;
                }
            }
        }
        
        // If not found in cookies, check Authorization header
        if (jwt == null) {
            String authHeader = req.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                jwt = authHeader.substring(7);
                log.info("RateLimitFilter - JWT token found in Authorization header");
            }
        }

        // If no JWT token found, skip rate limiting (let other filters handle authentication)
        if (jwt == null) {
            log.info("RateLimitFilter - No JWT token found, skipping rate limiting");
            chain.doFilter(request, response);
            return;
        }

        // TODO: remove this in prod
        if (jwt.equals("THEMUS_ADMIN")) {
            log.info("RateLimitFilter - User is admin, skipping rate limiting");
            chain.doFilter(request, response);
            return;
        }
        
        // Validate JWT first
        if (!jwtService.validateToken(jwt)) {
            log.warn("RateLimitFilter - Invalid JWT token provided");
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.getWriter().write("{\"error\":\"Invalid token\"}");
            return;
        }

        String email = jwtService.extractUsername(jwt);
        log.info("RateLimitFilter - Checking rate limit for user: {}", email);
        
        // Implement the rate limiting algorithm from pseudocode
        if (!isRequestAllowed(email)) {
            // Line 3: Show error message and end connection
            log.warn("RateLimitFilter - Rate limit exceeded for user: {}", email);
            res.setStatus(429); // HTTP 429 Too Many Requests
            res.setContentType("application/json");
            res.getWriter().write("{\"error\":\"Rate limit exceeded. Maximum " + RATE_LIMIT_PER_MINUTE + " requests per minute.\"}");
            return;
        }

        // Line 5: Allow request - do service stuff (continue to next filter)
        log.info("RateLimitFilter - Request allowed for user: {}", email);
        chain.doFilter(request, response);
    }

    private boolean isRequestAllowed(String email) {
        // Get current minute number (timestamp divided by 60 seconds)
        long currentMinute = System.currentTimeMillis() / (60 * 1000);
        String rateLimitKey = RATE_LIMIT_KEY_PREFIX + email + ":" + currentMinute;

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
            log.error("RateLimitFilter - Redis error, allowing request: {}", e.getMessage());
            // In case of Redis error, allow the request (fail open)
            return true;
        }
    }
}
