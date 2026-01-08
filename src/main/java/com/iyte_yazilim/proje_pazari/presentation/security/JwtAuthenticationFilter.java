package com.iyte_yazilim.proje_pazari.presentation.security;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

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
            if (jwtUtil.validateToken(jwt)) {
                // Extract all user info from JWT - NO database lookup!
                UserPrincipal userPrincipal = jwtUtil.extractUserPrincipal(jwt);

                // 2. Perform your Blacklist Check (Keep this from HEAD)
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    filterChain.doFilter(request, response);
                    return;
                }

                // 3. Create Authentication using Principal (Keep this from Dev/Incoming)
                // This avoids the database call 'loadUserByUsername'
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userPrincipal, null, userPrincipal.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (JwtException e) {
            log.warn(
                    "JWT authentication failed due to invalid token: {}",
                    e.getClass().getSimpleName());
        } catch (UsernameNotFoundException e) {
            log.warn("JWT authentication failed: {}", e.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }
}
