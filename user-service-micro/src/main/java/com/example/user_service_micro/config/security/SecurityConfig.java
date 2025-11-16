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

import com.example.user_service_micro.config.jwtEntry.JwtAuthEntryPoint;

/*
When we run the spring boot application, the classes that are annotated with @Configuration is loaded.
SecurityConfig builds and registers the Spring Security filter chain via

@Configuration tells spring to mark this class and register it as a source of beans.

@Bean
public SecurityFilterChain filterChain(HttpSecurity http)

This chain decides:
    - Which URLs are public or protected
    - What filters to apply

                SecurityConfig → defines which URLs require authentication, and which roles can access what
                JwtFilter → reads the Authorization header, validates the JWT, sets authentication context.
                JwtAuthEntryPoint → handles 401 errors when JWT is invalid, expired, or missing.

 */


/*
@EnableMethodSecurity tells Spring Security:

“In addition to the URL-based access rules you’ve defined in SecurityConfig,
also look for method-level security annotations like
@PreAuthorize, @PostAuthorize, @Secured, etc., and enforce them.”

        YES, we can safely remove it if we are not using any method-level security annotations.
 */

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired // Annotation used to perform dependency injection.
    private JwtFilter jwtFilter;

    @Autowired
    private JwtAuthEntryPoint jwtAuthEntryPoint;

    @Autowired
    private ServiceTokenFilter serviceTokenFilter;


    @Bean // indicates that this method creates and  returns a bean.
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http // Starts the HttpSecurity configuration chain.
                .csrf(csrf -> csrf.disable())
                /*
                CSRF(Cross-Site Request Forgery) only protects applications that use browser cookies for authentication.
                But in your system:
✅ You use JWT tokens
✅ Tokens are sent in the Authorization header, not cookies
✅ Your API is stateless (no sessions)

Why by default spring security enables CSRF ?
    - spring security assumes that we are using session based authentication (with cookies), so it enables
      CSRF by default to prevent these attacks.
    - But, we disable because we are using JWT tokens, and they are stateless.

                 */
                .authorizeHttpRequests(auth -> auth
                        // ✅ Allow all requests under /api/auth (signup, login, etc.)
                        .requestMatchers("/", "/api/auth/**").permitAll()
                        // Admin endpoints
                        .requestMatchers("/api/internal/**").permitAll()
                        .requestMatchers("/api/wallet/admin/**").hasRole("ADMIN")
                        // All others need authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthEntryPoint) // ✅ IMPORTANT
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(serviceTokenFilter, UsernamePasswordAuthenticationFilter.class) // first because, we are checking if there are any internal service calls.
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