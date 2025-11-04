package com.example.user_service_micro.config.jwt;

import com.example.user_service_micro.model.user.User;
import com.example.user_service_micro.repository.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
/*
JwtFilter runs before every secured request, this class runs once per HTTP request and checks :
    Does this request have a valid JWT token? If yes, authenticate the user.

    @Component → Makes Spring automatically detect and register this filter as a bean.

    OncePerRequestFilter → Ensures it runs exactly once per request
 */
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    /*
    JwtUtil helps validate and extract data from the token.
    UserRepository fetches the user from DB (to set authorities/roles).
     */

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Skip public endpoints
        if (path.startsWith("/api/auth") || path.equals("/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ Extract token
        /*
        It fetches the Authorization header from the HTTP request.

        For example, if the request contains:
        Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...

        then authHeader will contain that entire string:
        "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

         */
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = authHeader.substring(7);

        // ✅ Validate token
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // ✅ Fetch user and set authentication
        User user = userRepository.findByEmail(jwtUtil.getEmailFromToken(token)).orElse(null);
        if (user != null) {
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(user, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}

/*
@Component vs @Service

@Component : Makes spring automatically detect and register this class as a spring managed bean.
                                            vs
@Service : Also marks a class as a Spring-managed bean, but with semantic meaning — it represents
the class as a service layer component (business logic)
 */