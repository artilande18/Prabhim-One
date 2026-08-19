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
                "/api/v1/auth/logout/",
                "/api/v1/auth/sessions/",
                "/api/v1/auth/logout-all/",
                "/api/invoices/",
                "/api/invoices/{id}/",
                "/api/invoices/{id}/mark-sent/",
                "/api/customers/",
                "/api/customers/{id}/",
                "/api/customers/{id}/restore/",
                "/api/customers/{id}/deactivate/",
                "/api/customers/{id}/activate/",
                "/api/products/",
                "/api/products/{id}/",
                "/api/products/{id}/restore/",
                "/api/products/{id}/deactivate/",
                "/api/products/{id}/activate/",
                "/api/products/bulk-delete/",
                "/api/products/bulk-activate/",
                "/api/products/bulk-deactivate/",
                "/error" //  allow error forwarding
            ).permitAll()
            .anyRequest().authenticated());

        return http.build();
    }

     @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }  
}
