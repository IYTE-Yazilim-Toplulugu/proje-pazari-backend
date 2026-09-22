package com.iyte_yazilim.proje_pazari.presentation.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/** Prevents broker data from reaching sessions whose access token expired or was revoked. */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketOutboundSecurityInterceptor implements ChannelInterceptor {

    private final WebSocketSecurityInterceptor webSocketSecurityInterceptor;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor =
                SimpMessageHeaderAccessor.getAccessor(message, SimpMessageHeaderAccessor.class);
        if (accessor == null || accessor.getMessageType() != SimpMessageType.MESSAGE) {
            return message;
        }

        try {
            webSocketSecurityInterceptor.authenticateSession(accessor.getSessionId());
            return message;
        } catch (AuthenticationException exception) {
            log.debug("Dropping WebSocket message for an expired or revoked session");
            return null;
        }
    }
}
