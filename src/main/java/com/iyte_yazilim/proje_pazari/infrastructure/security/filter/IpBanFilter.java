package com.iyte_yazilim.proje_pazari.infrastructure.security.filter;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.BannedIpEntity;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Filter that blocks requests from banned IP addresses. Returns 403 Forbidden with a JSON response.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(0)
public class IpBanFilter extends OncePerRequestFilter {

    private final BannedIpRepository bannedIpRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIp(request);

        if (isIpBanned(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            String jsonResponse =
                    """
                    {
                        "code": "FORBIDDEN",
                        "message": "Your IP address has been banned.",
                        "data": null
                    }
                    """;

            response.getWriter().write(jsonResponse);
            log.warn("Blocked request from banned IP: {}", clientIp);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isIpBanned(String ip) {
        try {
            Optional<BannedIpEntity> banned = bannedIpRepository.findByIpAddress(ip);
            if (banned.isEmpty()) {
                return false;
            }
            BannedIpEntity ban = banned.get();
            // Check if ban has expired
            if (ban.getExpiresAt() != null && ban.getExpiresAt().isBefore(LocalDateTime.now())) {
                return false;
            }
            return true;
        } catch (Exception e) {
            log.warn("Failed to check IP ban status for: {}", ip, e);
            return false;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
