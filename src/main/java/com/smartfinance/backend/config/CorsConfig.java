package com.smartfinance.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Production CORS Configuration for FinvisIQ Backend.
 * Seamlessly connects the Netlify frontend (https://finvis.netlify.app)
 * and Railway Spring Boot backend over HTTPS, with full preflight OPTIONS support.
 */
@Configuration
public class CorsConfig {

    @Value("${FRONTEND_URL:https://finvis.netlify.app}")
    private String frontendUrl;

    @Value("${finvisiq.cors.allowed-origins:https://finvis.netlify.app}")
    private String allowedOrigins;

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);

        // Always allow production Netlify frontend origin (no trailing slash)
        config.addAllowedOrigin("https://finvis.netlify.app");

        // Add FRONTEND_URL if provided via environment variable
        if (frontendUrl != null && !frontendUrl.isBlank()) {
            String cleanFrontend = frontendUrl.trim().replaceAll("/+$", "");
            config.addAllowedOrigin(cleanFrontend);
        }

        // Add any additional comma-separated origins from configuration
        if (allowedOrigins != null && !allowedOrigins.isBlank()) {
            List<String> origins = Arrays.stream(allowedOrigins.split(","))
                    .map(s -> s == null ? "" : s.trim().replaceAll("/+$", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            for (String origin : origins) {
                config.addAllowedOrigin(origin);
            }
        }

        // Allow preview and branch deploys on Netlify
        config.addAllowedOriginPattern("https://*.netlify.app");

        // Allow local development frontends
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://localhost:8080");
        config.addAllowedOrigin("http://localhost:8081");
        config.addAllowedOrigin("http://localhost:8085");
        config.addAllowedOrigin("http://127.0.0.1:3000");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://127.0.0.1:8080");
        config.addAllowedOrigin("http://127.0.0.1:8085");

        // Permitted HTTP methods
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Permitted headers (all headers allowed for preflight requests)
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "*"));

        // Exposed headers for client consumption
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition", "Content-Type", "X-Total-Count"));

        // Cache preflight OPTIONS response for 1 hour (3600 seconds)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
