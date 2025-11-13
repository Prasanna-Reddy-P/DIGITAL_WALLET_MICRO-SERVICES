package com.example.wallet_service_micro.config.jwt;

import com.example.wallet_service_micro.client.user.UserClient;
import com.example.wallet_service_micro.dto.user.UserDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final UserClient userClient;

    public JwtFilter(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, //HttpServletRequest is an interface that represents an HTTP request sent by a client to your server
                                    HttpServletResponse response, // Represents the HTTP response that your server will send back.
                                    FilterChain filterChain) // represents sequence of filters that request has to pass before it hits the controller, it can be JWT validation, and authorities check.
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Allow public endpoints
        if (path.equals("/") || path.startsWith("/api/public") || path.contains("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            // ✅ Get user info from user-service
            UserDTO user = userClient.getUserFromToken(authHeader);
            if (user != null && user.getEmail() != null) {
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
                var auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(request, response);
    }
}


/*
✅ What is UsernamePasswordAuthenticationToken?

It is a Spring Security authentication object used to represent:
✅ the authenticated user (principal)
✅ the user's password (after login) or null
✅ the user's authorities/roles (credentials)
 */