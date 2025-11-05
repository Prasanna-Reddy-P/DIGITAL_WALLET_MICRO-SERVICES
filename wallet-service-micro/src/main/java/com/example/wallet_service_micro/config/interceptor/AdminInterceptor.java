package com.example.wallet_service_micro.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminInterceptor implements HandlerInterceptor {
    /*
    The class implements HandlerInterceptor, which allows you to run custom logic before (or after) a controller handles a request.

This is commonly used for:
Authentication
Logging
Admin checks
Request validation
     */

    private static final String ADMIN_SECRET = "SuperSecretAdminKey123"; // match user-service
    //This is a hardcoded secret key used to verify admin requests.


    /*
    ✅ This method runs before the controller method is called.
You can:
Inspect the request (headers, URL, body)
Decide whether to continue or block the request
Returning true → request proceeds to controller
Returning false → request is blocked (controller never runs)
     */
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

/*
If your wallet-service has an admin API like:
GET /api/wallet/admin/users/1

Then your user-service must call it like:
GET http://localhost:8086/api/wallet/admin/users/1
X-ADMIN-SECRET: SuperSecretAdminKey123
Authorization: Bearer <admin_jwt_token>
Content-Type: application/json

 */
