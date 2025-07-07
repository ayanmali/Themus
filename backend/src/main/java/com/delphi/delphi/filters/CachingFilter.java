// package com.delphi.delphi.filters;

// import java.io.IOException;

// import org.springframework.core.annotation.Order;
// import org.springframework.stereotype.Component;

// import com.delphi.delphi.components.RedisService;

// import jakarta.servlet.Filter;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.ServletRequest;
// import jakarta.servlet.ServletResponse;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// use redis to cache responses for GET requests 
// @Component
// @Order(5)
// public class CachingFilter implements Filter {

//     private final RedisService redisService;

//     public CachingFilter(RedisService redisService) {
//         this.redisService = redisService;
//     }

//     @Override
//     public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//             throws IOException, ServletException {
//         HttpServletRequest req = (HttpServletRequest) request;
//         HttpServletResponse res = (HttpServletResponse) response;
//         chain.doFilter(request, response);
//     }
    
// }
