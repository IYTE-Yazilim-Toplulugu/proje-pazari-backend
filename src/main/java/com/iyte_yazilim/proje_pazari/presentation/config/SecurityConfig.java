package com.iyte_yazilim.proje_pazari.presentation.config;

import com.iyte_yazilim.proje_pazari.infrastructure.security.filter.IpBanFilter;
import com.iyte_yazilim.proje_pazari.infrastructure.security.filter.MaintenanceModeFilter;
import com.iyte_yazilim.proje_pazari.infrastructure.security.filter.RateLimitFilter;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtAuthenticationFilter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final IpBanFilter ipBanFilter;
    private final MaintenanceModeFilter maintenanceModeFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(
                        auth ->
                                auth
                                        // Allow CORS preflight requests through
                                        .requestMatchers(HttpMethod.OPTIONS, "/**")
                                        .permitAll()
                                        // Swagger/OpenAPI endpoints
                                        .requestMatchers(
                                                "/swagger-ui/**",
                                                "/swagger-ui",
                                                "/v3/api-docs/**",
                                                "/swagger-ui.html",
                                                "/swagger-resources/**",
                                                "/webjars/**")
                                        .permitAll()
                                        // Actuator health endpoints
                                        .requestMatchers("/actuator/**")
                                        .permitAll()
                                        .requestMatchers("/api/v1/health")
                                        .permitAll()
                                        // WebSocket endpoint
                                        .requestMatchers("/ws/**")
                                        .permitAll()
                                        // Public authentication endpoints
                                        .requestMatchers("/api/v1/auth/**")
                                        .permitAll()
                                        // File serving endpoints (profile pictures are public)
                                        .requestMatchers("/api/v1/files/**")
                                        .permitAll()
                                        // Search endpoints - public access for project discovery
                                        .requestMatchers(HttpMethod.GET, "/api/v1/search/**")
                                        .permitAll()
                                        // Public read-only endpoints - anyone can view user
                                        // profiles and projects
                                        .requestMatchers(
                                                HttpMethod.GET, "/api/v1/users", "/api/v1/users/**")
                                        .permitAll()
                                        .requestMatchers(
                                                HttpMethod.GET,
                                                "/api/v1/projects",
                                                "/api/v1/projects/**")
                                        .permitAll()
                                        // All other requests require authentication
                                        .anyRequest()
                                        .authenticated())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(ipBanFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(maintenanceModeFilter, IpBanFilter.class)
                .addFilterAfter(rateLimitFilter, MaintenanceModeFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, RateLimitFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(frontendUrl, "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
