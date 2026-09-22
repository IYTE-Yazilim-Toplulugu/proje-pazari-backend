package com.iyte_yazilim.proje_pazari.infrastructure.security.filter;

import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig;
import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig.Policy;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Redis-backed rate limiter. Applies per-(endpoint-class, IP) fixed-window counters. Survives
 * restarts and coordinates across multiple application instances.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String RATE_KEY_PREFIX = "rate:";

    private final StringRedisTemplate redisTemplate;

    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Policy policy = RateLimitConfig.matchPolicy(path);

        if (policy != null) {
            String clientIp = getClientIp(request);
            String key = RATE_KEY_PREFIX + policy.name() + ":" + clientIp;

            try {
                Long count = redisTemplate.opsForValue().increment(key);
                if (count != null && count == 1) {
                    redisTemplate.expire(key, Duration.ofSeconds(policy.windowSeconds));
                }

                if (count != null && count > policy.limit) {
                    Long ttlSeconds = redisTemplate.getExpire(key);
                    long retryAfter =
                            ttlSeconds != null && ttlSeconds > 0
                                    ? ttlSeconds
                                    : policy.windowSeconds;

                    log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    response.setHeader("Retry-After", String.valueOf(retryAfter));
                    response.getWriter()
                            .write(
                                    String.format(
                                            "{\"code\":\"TOO_MANY_REQUESTS\","
                                                    + "\"message\":\"Rate limit exceeded."
                                                    + " Please try again in %d seconds.\","
                                                    + "\"data\":null}",
                                            retryAfter));
                    return;
                }
            } catch (Exception e) {
                log.warn(
                        "Rate limiter unavailable for {} on {}: {}",
                        clientIp,
                        path,
                        e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
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
