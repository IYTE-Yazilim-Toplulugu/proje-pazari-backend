package com.iyte_yazilim.proje_pazari.infrastructure.security.filter;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that blocks all non-admin, non-auth requests when maintenance mode is active. Returns 503
 * Service Unavailable with a JSON response.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class MaintenanceModeFilter extends OncePerRequestFilter {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Always allow auth, swagger, actuator, and admin endpoints
        if (path.startsWith("/api/v1/auth")
                || path.startsWith("/api/v1/admin")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (isMaintenanceModeEnabled()) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            String jsonResponse =
                    """
                    {
                        "code": "SERVICE_UNAVAILABLE",
                        "message": "System is under maintenance. Please try again later.",
                        "data": null
                    }
                    """;

            response.getWriter().write(jsonResponse);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isMaintenanceModeEnabled() {
        try {
            Optional<SystemConfigEntity> config =
                    systemConfigRepository.findByConfigKey("maintenanceMode");
            return config.isPresent() && "true".equalsIgnoreCase(config.get().getConfigValue());
        } catch (Exception e) {
            log.warn("Failed to check maintenance mode status", e);
            return false;
        }
    }
}
