package com.crimeLink.analyzer.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
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

    @org.springframework.beans.factory.annotation.Value("${cors.allowed-origins:*}")
    private String corsAllowedOrigins;

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
                        .requestMatchers("/api/facial/health").permitAll()  // ML service health check
                        .requestMatchers("/api/call-analysis/health").permitAll()  // ML service health check
                        
                        // ML Service endpoints
                        .requestMatchers("/api/call-analysis/**").hasRole("Investigator")
                        .requestMatchers("/api/facial/register").hasAnyRole("Investigator", "OIC")
                        .requestMatchers("/api/facial/criminals").hasAnyRole("Investigator", "OIC")
                        .requestMatchers("/api/facial/**").hasRole("Investigator")

                        // Criminal CRUD (direct DB, no Python)
                        .requestMatchers("/api/criminals/**").hasAnyRole("Investigator", "OIC")
                        .requestMatchers("/api/criminals").hasAnyRole("Investigator", "OIC")
                        
                        .requestMatchers("/api/database/**").permitAll()
                        .requestMatchers("/api/test").permitAll()
                        .requestMatchers("/api/debug/**").permitAll() // 🔍 Debug endpoints
                        .requestMatchers("/error").permitAll() // Allow error page without auth

                        // Public endpoints
                        .requestMatchers("/api/vehicle**").permitAll()
                        .requestMatchers("/api/mobile/auth/**").permitAll()
                        .requestMatchers("/api/duties/**").permitAll()
                        .requestMatchers("/api/crime-reports/map").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/crime-reports").permitAll()
                        .requestMatchers("/api/crime-reports/upload-evidence").authenticated()
                        .requestMatchers("/api/crime-reports/**").hasAnyRole("OIC", "Admin")

                        // Field Officer routes
                        .requestMatchers("/api/officers/me/**").hasRole("FieldOfficer")
                        .requestMatchers("/api/mobile/**").hasRole("FieldOfficer")
                        .requestMatchers("/api/leaves/**").permitAll()

                        // OIC-only routes
                        .requestMatchers("/api/duty-schedules/**").hasRole("OIC")
                        .requestMatchers("/api/weapon/**").hasRole("OIC")
                        .requestMatchers("/api/weapon-issue/**").hasRole("OIC")

                        // Admin/OIC/Investigator routes (officer data, locations, users)
                        .requestMatchers("/api/users/field-officers").hasAnyRole("Admin", "OIC", "Investigator")
                        .requestMatchers("/api/admin/officers/*/locations/**").hasAnyRole("Admin", "OIC", "Investigator")

                        // Admin-only: backup, restore, settings (must come before general /api/admin/**)
                        .requestMatchers("/api/admin/backup").hasRole("Admin")
                        .requestMatchers("/api/admin/restore").hasRole("Admin")
                        .requestMatchers("/api/admin/backups").hasRole("Admin")
                        .requestMatchers("/api/admin/settings").hasRole("Admin")

                        .requestMatchers("/api/admin/**").hasAnyRole("OIC", "Admin")

                        .anyRequest().authenticated())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpStatus.FORBIDDEN.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"message\":\"Access denied\"}");
                        }))
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
        // Use env var CORS_ALLOWED_ORIGINS to configure origins
        // Default: * (all origins) — restrict for production
        if ("*".equals(corsAllowedOrigins)) {
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            configuration.setAllowedOrigins(
                Arrays.asList(corsAllowedOrigins.split(","))
            );
        }
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