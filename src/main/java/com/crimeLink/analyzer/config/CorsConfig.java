package com.crimeLink.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:*}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        // Use allowedOriginPatterns instead of allowedOrigins when credentials are enabled
        config.setAllowedOriginPatterns(parseAllowedOrigins(allowedOrigins));

        config.setAllowedHeaders(List.of("*"));
        // Allow specific HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        // Expose specific headers to the client
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        // Optional: Set max age for preflight requests (in seconds)
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }

    private List<String> parseAllowedOrigins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("*");
        }

        String[] parts = raw.split(",");
        List<String> origins = new ArrayList<>();
        for (String part : parts) {
            String origin = part.trim();
            if (!origin.isEmpty()) {
                origins.add(origin);
            }
        }

        return origins.isEmpty() ? List.of("*") : origins;
    }
}
