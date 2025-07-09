package com.delphi.delphi.filters;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

// Security filter
// TODO: enable this in production
@Component
@Order(2)
public class SecurityFilter implements Filter {

    // private final String[] BOT_AGENTS = {
    //     "postman",
    //     "curl",
    //     "insomnia",
    //     "requests",
    //     "httpclient",
    //     "httpie",
    //     "python-requests",
    //     "python-httpx",
    //     "python-httpcore",
    //     "python-httpx",
    // };
    // check for user agent and block if it's a bot
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        // HttpServletRequest req = (HttpServletRequest) request;
        // HttpServletResponse res = (HttpServletResponse) response;

        // // check for user agent and block if it's a bot
        // String userAgent = req.getHeader("User-Agent");
        // if (userAgent == null || Arrays.stream(BOT_AGENTS).anyMatch(userAgent::contains)) {
        //     res.setStatus(HttpStatus.FORBIDDEN.value());
        //     return;
        // }
        chain.doFilter(request, response);
    }
}
