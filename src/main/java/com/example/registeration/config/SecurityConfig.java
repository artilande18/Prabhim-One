package com.example.registeration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // Disable CSRF for REST APIs
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth


            .requestMatchers(
                "/api/v1/auth/register/",
                "/api/v1/auth/verify-email/",
                "/api/v1/auth/resend-otp/",
                "/api/v1/auth/login/",
                "/api/v1/auth/token/refresh/",
                "/api/v1/auth/forgot-password/",
                "/api/v1/auth/verify-reset-otp/",
                "/api/v1/auth/reset-password/",
                "/error"
            ).permitAll()

        
                .anyRequest().authenticated()
            );

        return http.build();
    }

     @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
}
