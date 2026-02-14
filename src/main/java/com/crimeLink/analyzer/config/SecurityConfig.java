package com.crimeLink.analyzer.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                // CRITICAL FIX: Enable CORS using the bean configuration
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/admin/health").permitAll()
                        .requestMatchers("/api/database/**").permitAll()
                        .requestMatchers("/api/test").permitAll()
                        .requestMatchers("/api/debug/**").permitAll() // 🔍 Debug endpoints

                        // Public endpoints
                        .requestMatchers("/api/vehicle**").permitAll()
                        .requestMatchers("/api/mobile/auth/**").permitAll()
                        .requestMatchers("/api/duties/**").permitAll()
                        .requestMatchers("/api/crime-reports/map").permitAll()

                        // Field Officer routes
                        .requestMatchers("/api/officers/me/**").hasRole("FieldOfficer")
                        .requestMatchers("/api/mobile/**").hasRole("FieldOfficer")
                        .requestMatchers("/api/leaves/**").permitAll()

                        // OIC-only routes
                        .requestMatchers("/api/duty-schedules/**").hasRole("OIC")
                        .requestMatchers("/api/weapon/**").hasRole("OIC")
                        .requestMatchers("/api/weapon-issue/**").hasRole("OIC")

                        // Admin/OIC routes (officer data, locations, users)
                        .requestMatchers("/api/users/field-officers").hasAnyRole("Admin", "OIC")
                        .requestMatchers("/api/admin/**").hasAnyRole("OIC", "Admin")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Use allowedOriginPatterns for wildcard support with credentials
        // For production, replace with specific origins
        configuration.setAllowedOriginPatterns(List.of("*"));
        // Or use specific origins (recommended for production):
        // configuration.setAllowedOrigins(Arrays.asList(
        // "http://localhost:5173",
        // "http://localhost:3000",
        // "https://yourdomain.com"
        // ));
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}