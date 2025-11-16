package com.example.user_service_micro.config.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServiceTokenFilter extends OncePerRequestFilter {

    @Value("${internal.service-token}")
    private String serviceToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ---------------------------------------------------
        // ✅ Skip for Swagger & Public Auth APIs
        // ---------------------------------------------------
        if (path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.equals("/swagger-ui.html") ||
                path.startsWith("/api/auth") ||
                path.equals("/")) {

            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("X-Internal-Service-Key");

        if (header != null && header.equals(serviceToken)) {
            // ✅ Bypass security for internal trusted calls
            request.setAttribute("internalService", true);
        }

        filterChain.doFilter(request, response);
    }
}
