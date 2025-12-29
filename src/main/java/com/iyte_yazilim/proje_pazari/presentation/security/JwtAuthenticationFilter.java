package com.iyte_yazilim.proje_pazari.presentation.security;

import com.iyte_yazilim.proje_pazari.domain.security.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT authentication filter that processes every request.
 *
 * <p>This filter intercepts incoming HTTP requests and:
 *
 * <ol>
 *   <li>Extracts the JWT token from the Authorization header
 *   <li>Validates the token using {@link JwtUtil}
 *   <li>Loads user details from the database
 *   <li>Sets the authentication in Spring Security context
 * </ol>
 *
 * <h2>Authorization Header Format:</h2>
 *
 * <pre>
 * Authorization: Bearer {jwt_token}
 * </pre>
 *
 * <h2>Security Notes:</h2>
 *
 * <ul>
 *   <li>Invalid tokens are logged but do not stop the filter chain
 *   <li>Unauthenticated requests continue without authentication set
 *   <li>Security context is only set for valid, non-expired tokens
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see JwtUtil
 * @see CustomUserDetailsService
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Processes each request for JWT authentication.
     *
     * <p>Extracts and validates the JWT token from the Authorization header, then sets the Spring
     * Security authentication context if valid.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(
            @Nonnull HttpServletRequest request,
            @Nonnull HttpServletResponse response,
            @Nonnull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);

        try {
            // Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtUtil.validateToken(jwt)) {
                // Extract all user info from JWT - NO database lookup!
                UserPrincipal userPrincipal = jwtUtil.extractUserPrincipal(jwt);

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException e) {
            // JWT validation failed (malformed token, expired token, invalid signature,
            // etc.)
            // Log for security monitoring but continue filter chain without authentication
            log.warn(
                    "JWT authentication failed due to invalid token: {}",
                    e.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }
}
