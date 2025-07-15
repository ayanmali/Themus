package com.delphi.delphi.filters;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// Security filter
@Component
@Order(3) // Run after JWT filter
public class SecurityFilter implements Filter {

    private final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    private final String[] BOT_AGENTS = {
        "postman",
        "curl", 
        "insomnia",
        "python-requests",
        "python-httpx",
        "python-httpcore",
        "httpie",
        "wget",
        "httpclient/4"
    };
    
    // check for user agent and block if it's a bot
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        // check for user agent and block if it's a bot
        // String userAgent = req.getHeader("User-Agent");
        // String requestPath = req.getRequestURI();
        
        // log.info("SecurityFilter - Processing request to: {} with User-Agent: {}", requestPath, userAgent);
        
        // // Allow requests to auth endpoints regardless of user agent
        // if (requestPath.startsWith("/api/auth/")) {
        //     log.info("SecurityFilter - Allowing auth endpoint: {}", requestPath);
        //     chain.doFilter(request, response);
        //     return;
        // }
        
        // // Be more permissive - only block if user agent explicitly matches known bots
        // if (userAgent != null && Arrays.stream(BOT_AGENTS).anyMatch(agent -> userAgent.toLowerCase().contains(agent.toLowerCase()))) {
        //     res.setStatus(HttpStatus.FORBIDDEN.value());
        //     log.warn("SecurityFilter - Blocked request from bot: {} to path: {}", userAgent, requestPath);
        //     return;
        // }
        
        // log.info("SecurityFilter - Allowing request from: {} to path: {}", userAgent, requestPath);
        chain.doFilter(request, response);
    }
}
