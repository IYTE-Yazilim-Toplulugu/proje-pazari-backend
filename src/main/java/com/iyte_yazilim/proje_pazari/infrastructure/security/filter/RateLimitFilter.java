package com.iyte_yazilim.proje_pazari.infrastructure.security.filter;

import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldRateLimit(path)) {
            String clientIp = getClientIp(request);
            Bucket bucket = rateLimitConfig.resolveBucket(clientIp);

            ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

            if (probe.isConsumed()) {
                response.addHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
                filterChain.doFilter(request, response);
            } else {
                long waitForRefill = probe.getNanosToWaitForRefill() / 1_000_000_000;
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.addHeader("X-Rate-Limit-Retry-After-Seconds", String.valueOf(waitForRefill));

                String jsonResponse =
                        String.format(
                                """
                        {
                            "code": "TOO_MANY_REQUESTS",
                            "message": "Rate limit exceeded. Please try again in %d seconds.",
                            "data": null
                        }
                        """,
                                waitForRefill);

                response.getWriter().write(jsonResponse);
                log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            }
        } else {
            filterChain.doFilter(request, response);
        }
    }

    private boolean shouldRateLimit(String path) {
        return path.startsWith("/api/v1/auth/login") || path.startsWith("/api/v1/auth/register");
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
