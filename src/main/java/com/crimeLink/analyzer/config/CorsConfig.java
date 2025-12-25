package com.crimeLink.analyzer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        // Allow credentials (cookies, authorization headers, etc.)
        config.setAllowCredentials(true);
        // Use allowedOriginPatterns instead of allowedOrigins when credentials are enabled
        config.setAllowedOriginPatterns(List.of("*"));

        config.setAllowedHeaders(List.of("*"));
        // Allow specific HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        // Expose specific headers to the client
        config.setExposedHeaders(List.of("Authorization"));
        // Optional: Set max age for preflight requests (in seconds)
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}