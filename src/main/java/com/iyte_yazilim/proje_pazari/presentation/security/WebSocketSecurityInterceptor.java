package com.iyte_yazilim.proje_pazari.presentation.security;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Authenticates STOMP sessions with the existing access JWT and applies a deny-by-default inbound
 * destination policy.
 *
 * <p>Browser WebSocket and SockJS handshakes cannot reliably attach an HTTP Authorization header,
 * so authentication is performed on the STOMP CONNECT frame. The token is kept only in the
 * server-side WebSocket session and revalidated for every subsequent data-bearing client frame so
 * expiry and logout/deactivation blacklists take effect without reconnecting.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_LOWERCASE = "authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ADMIN_TOPIC_PREFIX = "/topic/admin/";
    private static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final ConcurrentMap<String, String> sessionTokens = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                StompHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            // Heartbeats do not carry a STOMP command or application data.
            return message;
        }

        StompCommand command = accessor.getCommand();
        switch (command) {
            case CONNECT, STOMP -> authenticateConnect(accessor);
            case SUBSCRIBE -> authorizeSubscription(accessor);
            case UNSUBSCRIBE -> restoreAndValidateAuthentication(accessor);
            case DISCONNECT -> removeSession(accessor.getSessionId());
            default -> denyClientCommand(accessor);
        }

        return message;
    }

    private void authenticateConnect(StompHeaderAccessor accessor) {
        String authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (authorization == null) {
            authorization = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER_LOWERCASE);
        }

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("A Bearer access token is required");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();
        if (token.isEmpty()) {
            throw new BadCredentialsException("A Bearer access token is required");
        }

        Authentication authentication = authenticate(token);
        String sessionId = accessor.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new BadCredentialsException("WebSocket session identifier is unavailable");
        }

        sessionTokens.put(sessionId, token);
        accessor.setUser(authentication);
    }

    private void authorizeSubscription(StompHeaderAccessor accessor) {
        Authentication authentication = restoreAndValidateAuthentication(accessor);
        String destination = accessor.getDestination();

        if (destination == null || !destination.startsWith(ADMIN_TOPIC_PREFIX)) {
            throw new AccessDeniedException("STOMP subscription destination is not allowed");
        }

        boolean isAdmin =
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> ADMIN_AUTHORITY.equals(authority.getAuthority()));
        if (!isAdmin) {
            throw new AccessDeniedException("ADMIN authority is required for this subscription");
        }
    }

    private void denyClientCommand(StompHeaderAccessor accessor) {
        // Validate first so anonymous clients receive an authentication failure rather than policy
        // details. The application currently exposes no client SEND destinations.
        restoreAndValidateAuthentication(accessor);
        throw new AccessDeniedException(
                "Client STOMP command is not allowed: "
                        + accessor.getCommand().name().toLowerCase(Locale.ROOT));
    }

    private Authentication restoreAndValidateAuthentication(StompHeaderAccessor accessor) {
        Authentication authentication = authenticateSession(accessor.getSessionId());
        accessor.setUser(authentication);
        return authentication;
    }

    Authentication authenticateSession(String sessionId) {
        String token = sessionId != null ? sessionTokens.get(sessionId) : null;
        if (token == null || token.isBlank()) {
            throw new BadCredentialsException("WebSocket session is not authenticated");
        }
        return authenticate(token);
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        removeSession(event.getSessionId());
    }

    private void removeSession(String sessionId) {
        if (sessionId != null) {
            sessionTokens.remove(sessionId);
        }
    }

    private Authentication authenticate(String token) {
        try {
            if (tokenBlacklistService.isTokenBlacklisted(token) || !jwtUtil.validateToken(token)) {
                throw new BadCredentialsException("WebSocket access token is invalid or expired");
            }

            UserPrincipal principal = jwtUtil.extractUserPrincipal(token);
            if (principal.getUserId() == null
                    || principal.getUserId().isBlank()
                    || principal.getUsername() == null
                    || principal.getUsername().isBlank()
                    || principal.getRole() == null
                    || principal.getRole().isBlank()) {
                throw new BadCredentialsException("WebSocket access token is missing claims");
            }

            if (tokenBlacklistService.isUserBlacklisted(principal.getUsername())) {
                throw new BadCredentialsException("WebSocket access token is invalid or expired");
            }

            return new UsernamePasswordAuthenticationToken(
                    principal, null, principal.getAuthorities());
        } catch (BadCredentialsException exception) {
            throw exception;
        } catch (JwtException | IllegalArgumentException exception) {
            log.warn(
                    "WebSocket JWT authentication failed: {}",
                    exception.getClass().getSimpleName());
            throw new BadCredentialsException("WebSocket access token is invalid or expired");
        }
    }
}
