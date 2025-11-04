package com.example.wallet_service_micro.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    private static final String ADMIN_SECRET = "SuperSecretAdminKey123"; // match user-service

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        System.out.println("🔍 AdminInterceptor triggered for: " + request.getRequestURI());

        String secretKey = request.getHeader("X-ADMIN-SECRET");
        if (secretKey == null || !secretKey.equals(ADMIN_SECRET)) {
            System.out.println("🚫 AdminInterceptor blocked request: invalid or missing secret key");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Unauthorized: Missing or invalid admin secret key");
            return false;
        }

        System.out.println("✅ AdminInterceptor passed");
        return true;
    }

}

