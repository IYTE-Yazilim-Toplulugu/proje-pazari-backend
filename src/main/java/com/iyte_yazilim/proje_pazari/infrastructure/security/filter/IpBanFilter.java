package com.iyte_yazilim.proje_pazari.infrastructure.security.filter;

import com.iyte_yazilim.proje_pazari.application.services.BanCheckService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

    private final BanCheckService banCheckService;

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
            return banCheckService.isIpBanned(ip);
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
