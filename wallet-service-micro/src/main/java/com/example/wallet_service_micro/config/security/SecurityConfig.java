package com.example.wallet_service_micro.config.security;

import com.example.wallet_service_micro.config.jwt.JwtFilter;
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

    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .requestMatchers("/", "/api/wallet/public/**").permitAll()
                        .requestMatchers("/api/wallet/admin/**").hasRole("ADMIN") // ✅ changed
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                /*
                By default, Spring Security creates and stores a user session after login.
                But in JWT-based APIs, we don’t want that — the token itself carries authentication data.
                Do not create or use HTTP sessions — every request must include a valid token.
                This ensures complete statelessness — each request stands alone
                 */
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
                /*
                Insert my custom jwtFilter before the UsernamePasswordAuthenticationFilter in the filter chain

                So the JWT filter:
                1) Extracts the token from the Authorization header.
                2) Validates it.
                3) If valid, sets the user details in the SecurityContext.
                4) Then the rest of the filters continue.
                 */

        return http.build();
        // Converts your config into a working SecurityFilterChain
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}

//  Cross-Site Request Forgery protection.

