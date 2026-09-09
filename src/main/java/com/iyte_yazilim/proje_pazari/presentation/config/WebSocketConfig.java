package com.iyte_yazilim.proje_pazari.presentation.config;

import com.iyte_yazilim.proje_pazari.presentation.security.WebSocketOutboundSecurityInterceptor;
import com.iyte_yazilim.proje_pazari.presentation.security.WebSocketSecurityInterceptor;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for real-time admin activity dashboard. Configures STOMP over SockJS with
 * a simple in-memory message broker.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketSecurityInterceptor webSocketSecurityInterceptor;
    private final WebSocketOutboundSecurityInterceptor webSocketOutboundSecurityInterceptor;
    private final List<String> allowedOrigins;

    public WebSocketConfig(
            WebSocketSecurityInterceptor webSocketSecurityInterceptor,
            WebSocketOutboundSecurityInterceptor webSocketOutboundSecurityInterceptor,
            @Value("${app.websocket.allowed-origins:http://localhost:3000}")
                    List<String> allowedOrigins) {
        this.webSocketSecurityInterceptor = webSocketSecurityInterceptor;
        this.webSocketOutboundSecurityInterceptor = webSocketOutboundSecurityInterceptor;
        this.allowedOrigins =
                allowedOrigins.stream()
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toList();
        if (this.allowedOrigins.isEmpty()) {
            throw new IllegalArgumentException(
                    "app.websocket.allowed-origins must contain at least one explicit origin");
        }
        if (this.allowedOrigins.stream().anyMatch(origin -> origin.contains("*"))) {
            throw new IllegalArgumentException(
                    "app.websocket.allowed-origins must not contain wildcard origins");
        }
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketSecurityInterceptor);
    }

    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketOutboundSecurityInterceptor);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.toArray(String[]::new))
                .withSockJS();
    }
}
