package com.example.user_service_micro.config.security;

import com.example.user_service_micro.config.jwt.JwtFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired // Annotation used to perform dependency injection.
    private JwtFilter jwtFilter;

    @Bean // indicates that this method returns a bean.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http // Starts the HttpSecurity configuration chain.
                .csrf(csrf -> csrf.disable())
                /*
                CSRF only protects applications that use browser cookies for authentication.
                But in your system:
✅ You use JWT tokens
✅ Tokens are sent in the Authorization header, not cookies
✅ Your API is stateless (no sessions)

                 */
                .authorizeHttpRequests(auth -> auth
                        // ✅ Allow all requests under /api/auth (signup, login, etc.)
                        .requestMatchers("/", "/api/auth/**").permitAll()
                        // Admin endpoints
                        .requestMatchers("/api/wallet/admin/**").hasRole("ADMIN")
                        // All others need authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
                /*
                Do not create or use HTTP sessions, Every request must authenticate itself with a JWT token.

                In a stateless API:

The server does not store any user login information.
The server does not remember who you are between requests.
Every request must send authentication credentials (like a JWT token).
                 */

        return http.build();
    }


    // Create a bean from this method and put it into the application context.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
/*

AuthenticationManager → the interface return type.

This is the core Spring Security component that performs authentication:
checks username/password
loads UserDetails
verifies credentials
returns an Authentication object if successful

authenticationManager → method name.


What is AuthenticationConfiguration?

A Spring Security class that knows how to build the application's AuthenticationManager.
It collects:

your UserDetailsService
your PasswordEncoder
all authentication providers

return authConfig.getAuthenticationManager();
Spring, give me the fully configured AuthenticationManager (based on your UserDetailsService and PasswordEncoder).
I will expose it as a bean.
 */