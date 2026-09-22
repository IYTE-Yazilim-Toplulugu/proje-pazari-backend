package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.BadCredentialsException;

@ExtendWith(MockitoExtension.class)
class WebSocketOutboundSecurityInterceptorTest {

    @Mock private WebSocketSecurityInterceptor webSocketSecurityInterceptor;
    @Mock private MessageChannel channel;

    private WebSocketOutboundSecurityInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new WebSocketOutboundSecurityInterceptor(webSocketSecurityInterceptor);
    }

    @Test
    void authenticatedSessionReceivesBrokerMessage() {
        Message<byte[]> message = message(SimpMessageType.MESSAGE, "session-id");

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
        verify(webSocketSecurityInterceptor).authenticateSession("session-id");
    }

    @Test
    void expiredOrRevokedSessionDoesNotReceiveBrokerMessage() {
        Message<byte[]> message = message(SimpMessageType.MESSAGE, "session-id");
        doThrow(new BadCredentialsException("expired"))
                .when(webSocketSecurityInterceptor)
                .authenticateSession("session-id");

        Message<?> result = interceptor.preSend(message, channel);

        assertNull(result);
    }

    @Test
    void protocolControlMessageIsNotTreatedAsBrokerData() {
        Message<byte[]> message = message(SimpMessageType.CONNECT_ACK, "session-id");

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
    }

    private Message<byte[]> message(SimpMessageType type, String sessionId) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create(type);
        accessor.setSessionId(sessionId);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
