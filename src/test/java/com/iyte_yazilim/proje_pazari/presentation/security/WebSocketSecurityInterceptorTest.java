package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class WebSocketSecurityInterceptorTest {

    private static final String ADMIN_TOKEN = "admin-token";
    private static final String USER_TOKEN = "user-token";

    @Mock private JwtUtil jwtUtil;
    @Mock private TokenBlacklistService tokenBlacklistService;
    @Mock private MessageChannel channel;

    private WebSocketSecurityInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketSecurityInterceptor(jwtUtil, tokenBlacklistService);
    }

    @Test
    void anonymousConnectIsRejected() {
        Frame frame = frame(StompCommand.CONNECT, null, new HashMap<>());

        assertThrows(
                BadCredentialsException.class, () -> interceptor.preSend(frame.message(), channel));
    }

    @Test
    void malformedBearerHeaderIsRejected() {
        Frame frame = frame(StompCommand.CONNECT, null, new HashMap<>());
        frame.accessor().setNativeHeader("Authorization", "Basic credentials");

        assertThrows(
                BadCredentialsException.class, () -> interceptor.preSend(frame.message(), channel));
    }

    @Test
    void blacklistedTokenIsRejectedWithoutParsingClaims() {
        when(tokenBlacklistService.isTokenBlacklisted(USER_TOKEN)).thenReturn(true);
        Frame frame = connectFrame(USER_TOKEN, new HashMap<>());

        assertThrows(
                BadCredentialsException.class, () -> interceptor.preSend(frame.message(), channel));
    }

    @Test
    void blacklistedUserIsRejected() {
        UserPrincipal principal = principal("USER");
        stubValidToken(USER_TOKEN, principal);
        when(tokenBlacklistService.isUserBlacklisted(principal.getUsername())).thenReturn(true);
        Frame frame = connectFrame(USER_TOKEN, new HashMap<>());

        assertThrows(
                BadCredentialsException.class, () -> interceptor.preSend(frame.message(), channel));
    }

    @Test
    void tokenMissingRequiredClaimsIsRejected() {
        UserPrincipal principal = new UserPrincipal(null, "user@example.com", "USER");
        stubValidToken(USER_TOKEN, principal);
        Frame frame = connectFrame(USER_TOKEN, new HashMap<>());

        assertThrows(
                BadCredentialsException.class, () -> interceptor.preSend(frame.message(), channel));
    }

    @Test
    void lowercaseAuthorizationHeaderAuthenticatesConnect() {
        UserPrincipal principal = principal("ADMIN");
        stubValidToken(ADMIN_TOKEN, principal);
        Map<String, Object> session = new HashMap<>();
        Frame frame = frame(StompCommand.CONNECT, null, session);
        frame.accessor().setNativeHeader("authorization", "Bearer " + ADMIN_TOKEN);

        interceptor.preSend(frame.message(), channel);

        assertNotNull(interceptor.authenticateSession("test-session"));
        Authentication authentication = (Authentication) frame.accessor().getUser();
        assertNotNull(authentication);
        assertEquals(principal, authentication.getPrincipal());
    }

    @Test
    void adminCanSubscribeToAdminTopic() {
        Map<String, Object> session = authenticatedSession(ADMIN_TOKEN, principal("ADMIN"));
        Frame subscribe = frame(StompCommand.SUBSCRIBE, "/topic/admin/activity", session);

        interceptor.preSend(subscribe.message(), channel);

        Authentication authentication = (Authentication) subscribe.accessor().getUser();
        assertNotNull(authentication);
        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void regularUserCannotSubscribeToAdminTopic() {
        Map<String, Object> session = authenticatedSession(USER_TOKEN, principal("USER"));
        Frame subscribe = frame(StompCommand.SUBSCRIBE, "/topic/admin/activity", session);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(subscribe.message(), channel));
    }

    @Test
    void adminCannotSubscribeToUnmatchedTopic() {
        Map<String, Object> session = authenticatedSession(ADMIN_TOKEN, principal("ADMIN"));
        Frame subscribe = frame(StompCommand.SUBSCRIBE, "/topic/public", session);

        assertThrows(
                AccessDeniedException.class,
                () -> interceptor.preSend(subscribe.message(), channel));
    }

    @Test
    void clientSendIsDeniedByDefault() {
        Map<String, Object> session = authenticatedSession(ADMIN_TOKEN, principal("ADMIN"));
        Frame send = frame(StompCommand.SEND, "/app/admin", session);

        assertThrows(
                AccessDeniedException.class, () -> interceptor.preSend(send.message(), channel));
    }

    @Test
    void revokedTokenIsRejectedOnAFrameAfterConnect() {
        UserPrincipal principal = principal("ADMIN");
        Map<String, Object> session = authenticatedSession(ADMIN_TOKEN, principal);
        when(tokenBlacklistService.isTokenBlacklisted(ADMIN_TOKEN)).thenReturn(true);
        Frame subscribe = frame(StompCommand.SUBSCRIBE, "/topic/admin/activity", session);

        assertThrows(
                BadCredentialsException.class,
                () -> interceptor.preSend(subscribe.message(), channel));
    }

    @Test
    void disconnectIsAlwaysAllowedForCleanup() {
        authenticatedSession(ADMIN_TOKEN, principal("ADMIN"));
        Frame disconnect = frame(StompCommand.DISCONNECT, null, new HashMap<>());

        interceptor.preSend(disconnect.message(), channel);

        assertThrows(
                BadCredentialsException.class,
                () -> interceptor.authenticateSession("test-session"));
    }

    private Map<String, Object> authenticatedSession(String token, UserPrincipal principal) {
        stubValidToken(token, principal);
        Map<String, Object> session = new HashMap<>();
        Frame connect = connectFrame(token, session);
        interceptor.preSend(connect.message(), channel);
        return session;
    }

    private void stubValidToken(String token, UserPrincipal principal) {
        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractUserPrincipal(token)).thenReturn(principal);
    }

    private UserPrincipal principal(String role) {
        return new UserPrincipal("user-id", role.toLowerCase() + "@example.com", role);
    }

    private Frame connectFrame(String token, Map<String, Object> session) {
        Frame frame = frame(StompCommand.CONNECT, null, session);
        frame.accessor().setNativeHeader("Authorization", "Bearer " + token);
        return frame;
    }

    private Frame frame(
            StompCommand command, String destination, Map<String, Object> sessionAttributes) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        accessor.setSessionId("test-session");
        accessor.setSessionAttributes(sessionAttributes);
        if (destination != null) {
            accessor.setDestination(destination);
        }
        accessor.setLeaveMutable(true);
        Message<byte[]> message =
                MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
        return new Frame(accessor, message);
    }

    private record Frame(StompHeaderAccessor accessor, Message<byte[]> message) {}
}
