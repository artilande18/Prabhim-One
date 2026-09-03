package com.example.registeration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Disable CSRF for REST APIs
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/api/v1/auth/register/",
                "/api/v1/auth/verify-email/",
                "/api/v1/auth/resend-otp/",
                "/api/v1/auth/reset-password/",
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
                "/api/customers/by-gst/**",
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
                "/api/v1/vendors/",
                "/api/v1/vendors/{id}/",
                "/api/v1/purchases/",
                "/api/v1/purchases/{id}/",
                "/api/v1/purchase-orders/",
                "/api/v1/purchase-orders/{id}/",
                "/api/v1/purchase-orders/{id}/restore/",
                "/api/v1/purchase-orders/{id}/permanent/",
                "/api/v1/purchase-orders/{id}/convert-to-bill/",
                "/api/v1/bills/",
                "/api/v1/bills/{id}/",
                "/api/v1/estimates/",
                "/api/v1/estimates/{id}/",
                "/api/v1/proforma-invoices",
                "/api/v1/proforma-invoices/",
                "/api/v1/proforma-invoices/{id}",
                "/api/v1/proforma-invoices/{id}/",
                "/api/v1/profile/",
                "/api/v1/profile/change-password/",
                "/api/v1/profile/upload-picture/",
                "/api/v1/profile/picture/",
                "/api/v1/expenses/",
                "/api/v1/expenses/{id}/",
                "/api/v1/settings",
                "/api/v1/settings/",
                "/api/v1/payments",
                "/api/v1/payments/",
                "/api/v1/payments/{id}/",
                "/api/v1/credit-notes",
                "/api/v1/credit-notes/",
                "/api/v1/credit-notes/{id}/",
                "/api/v1/subscription/plans",
                "/api/v1/subscription/plans/",
                "/api/v1/subscription/current",
                "/api/v1/subscription/current/",
                "/api/v1/subscription/subscribe",
                "/api/v1/subscription/subscribe/",
                "/api/v1/subscription/cancel",
                "/api/v1/subscription/cancel/",
                "/api/v1/subscription/billing-history",
                "/api/v1/subscription/billing-history/",
                "/api/v1/reports/profit-loss",
                "/api/v1/reports/profit-loss/",
                "/api/v1/reports/sales-summary",
                "/api/v1/reports/sales-summary/",
                "/api/v1/reports/tax-summary",
                "/api/v1/reports/tax-summary/",
                "/api/v1/reports/receivables",
                "/api/v1/reports/receivables/",
                "/api/v1/reports/payables",
                "/api/v1/reports/payables/",
                "/api/v1/reports/**",
                "/error" //  allow error forwarding
            ).permitAll()
            .anyRequest().authenticated());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
            "http://localhost:5173", 
            "https://invoice.prabhimtechnologies.in"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
