package com.delphi.delphi.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.delphi.delphi.components.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// JWT authentication filter
@Component
@Order(1) // Ensure this runs before SecurityFilter
public class JwtAuthFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        String method = request.getMethod();
        log.info("Processing request: {} {}", method, requestPath);

        // Skip JWT validation for permitted endpoints
        if (requestPath.equals("/") || requestPath.startsWith("/api/auth/") || requestPath.startsWith("/api/recordings") || requestPath.startsWith("/api/assessments/live")) {
            log.info("Skipping JWT validation for permitted endpoint: {}", requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        // User is already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            log.info("User is already authenticated, skipping JWT validation");
            filterChain.doFilter(request, response);
            return;
        }

        // Read token from cookie instead of Authorization header
        String jwt = null;
        Cookie[] cookies = request.getCookies();
        log.info("Request cookies: {}", cookies != null ? cookies.length : 0);
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                log.info("Cookie found: {} = {}", cookie.getName(), cookie.getValue());
                if ("accessToken".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    log.info("JWT token found in cookies: {}", jwt);
                    break;
                }
            }
        }

        // check bearer header
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            jwt = bearerToken.substring(7);
            log.info("JWT token found in bearer header: {}", jwt);
        }

        // Check if JWT token exists before trying to extract username
        if (jwt == null) {
            log.error("Unauthorized access: No JWT token found in cookies for path: {}", requestPath);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized: No JWT token found");
            return;
        }

        String email = jwtService.extractUsername(jwt);
        log.info("Extracted email from JWT: {}", email);

        if (email == null || !jwtService.validateToken(jwt)) {
            log.error("Unauthorized access: Invalid JWT token for email: {} with jwt: {}", email, jwt);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Unauthorized: Invalid JWT token");
            return;
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken emailPasswordAuthToken = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(emailPasswordAuthToken);

        log.info("Successfully authenticated user: {}", email);
        filterChain.doFilter(request, response);
    }
}
