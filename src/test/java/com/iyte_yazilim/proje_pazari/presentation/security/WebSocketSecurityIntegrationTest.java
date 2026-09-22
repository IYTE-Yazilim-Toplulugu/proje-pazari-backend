package com.iyte_yazilim.proje_pazari.presentation.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.application.dtos.AdminActivityEvent;
import com.iyte_yazilim.proje_pazari.application.service.AdminActivityNotifier;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.RestTemplateXhrTransport;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import({TestRedisConfig.class, TestRateLimitConfig.class})
@TestPropertySource(
        properties = {
            "app.frontend.url=https://allowed.example",
            "app.websocket.allowed-origins=https://allowed.example"
        })
class WebSocketSecurityIntegrationTest {

    private static final String ALLOWED_ORIGIN = "https://allowed.example";

    @LocalServerPort private int port;

    @Autowired private JwtUtil jwtUtil;
    @Autowired private AdminActivityNotifier adminActivityNotifier;
    @Autowired private TokenBlacklistService tokenBlacklistService;

    private final List<WebSocketStompClient> clients = new ArrayList<>();
    private final List<StompSession> sessions = new ArrayList<>();

    @AfterEach
    void closeClients() {
        sessions.stream().filter(StompSession::isConnected).forEach(StompSession::disconnect);
        clients.forEach(WebSocketStompClient::stop);
    }

    @Test
    void anonymousClientCannotConnectToTheBroker() {
        WebSocketStompClient client = webSocketTransportClient();

        assertThrows(
                Exception.class,
                () -> connect(client, ALLOWED_ORIGIN, null).get(5, TimeUnit.SECONDS));
    }

    @Test
    void regularUserCannotSubscribeToAdminActivity() throws Exception {
        WebSocketStompClient client = webSocketTransportClient();
        String token = jwtUtil.generateToken("user-id", "user@example.com", "USER");
        TestSessionHandler sessionHandler = new TestSessionHandler();
        StompSession session =
                connect(client, ALLOWED_ORIGIN, token, sessionHandler).get(5, TimeUnit.SECONDS);
        sessions.add(session);

        session.subscribe(
                "/topic/admin/activity", new AdminActivityFrameHandler(new CountDownLatch(1)));

        assertNotNull(sessionHandler.failure().get(5, TimeUnit.SECONDS));
    }

    @Test
    void adminReceivesMinimalActivityPayload() throws Exception {
        WebSocketStompClient client = webSocketTransportClient();
        String token = jwtUtil.generateToken("admin-id", "admin@example.com", "ADMIN");
        StompSession session = connect(client, ALLOWED_ORIGIN, token).get(5, TimeUnit.SECONDS);
        sessions.add(session);

        CountDownLatch messageReceived = new CountDownLatch(1);
        AdminActivityFrameHandler frameHandler = new AdminActivityFrameHandler(messageReceived);
        session.subscribe("/topic/admin/activity", frameHandler);

        AuditLogEntity auditLog =
                AuditLogEntity.builder()
                        .action("ADMIN_UPDATE_USER")
                        .entityType("USER")
                        .performedBy("admin@example.com")
                        .status("SUCCESS")
                        .details("must-not-be-broadcast")
                        .timestamp(LocalDateTime.now())
                        .build();
        publishUntilReceived(auditLog, messageReceived);

        assertEquals(0, messageReceived.getCount());
        assertEquals("ADMIN_UPDATE_USER", frameHandler.payload().action());
        assertEquals("USER", frameHandler.payload().entityType());
        assertEquals("admin@example.com", frameHandler.payload().performedBy());
    }

    @Test
    void disallowedOriginCannotEstablishSockJsTransport() {
        WebSocketStompClient client = webSocketTransportClient();
        String token = jwtUtil.generateToken("admin-id", "admin@example.com", "ADMIN");

        assertThrows(
                Exception.class,
                () -> connect(client, "https://evil.example", token).get(5, TimeUnit.SECONDS));
    }

    @Test
    void revokedAdminSessionStopsReceivingBrokerEvents() throws Exception {
        WebSocketStompClient client = webSocketTransportClient();
        String token =
                jwtUtil.generateToken("revoked-admin-id", "revoked-admin@example.com", "ADMIN");
        StompSession session = connect(client, ALLOWED_ORIGIN, token).get(5, TimeUnit.SECONDS);
        sessions.add(session);

        CountDownLatch initialMessageReceived = new CountDownLatch(1);
        AdminActivityFrameHandler frameHandler =
                new AdminActivityFrameHandler(initialMessageReceived);
        session.subscribe("/topic/admin/activity", frameHandler);
        AuditLogEntity auditLog =
                AuditLogEntity.builder()
                        .action("ADMIN_REVOCATION_TEST")
                        .entityType("SYSTEM")
                        .performedBy("revoked-admin@example.com")
                        .status("SUCCESS")
                        .timestamp(LocalDateTime.now())
                        .build();
        publishUntilReceived(auditLog, initialMessageReceived);
        assertNotNull(frameHandler.next(1, TimeUnit.SECONDS));

        tokenBlacklistService.blacklistToken(token, Duration.ofMinutes(5));
        for (int attempt = 0; attempt < 5; attempt++) {
            adminActivityNotifier.notifyAdmins(auditLog);
            Thread.sleep(50);
        }

        assertNull(frameHandler.next(500, TimeUnit.MILLISECONDS));
    }

    @Test
    void xhrFallbackUsesTheSameAdminAuthorization() throws Exception {
        WebSocketStompClient client = xhrTransportClient();
        String token = jwtUtil.generateToken("admin-id", "admin@example.com", "ADMIN");
        StompSession session = connect(client, ALLOWED_ORIGIN, token).get(10, TimeUnit.SECONDS);
        sessions.add(session);

        CountDownLatch messageReceived = new CountDownLatch(1);
        session.subscribe("/topic/admin/activity", new AdminActivityFrameHandler(messageReceived));
        AuditLogEntity auditLog =
                AuditLogEntity.builder()
                        .action("ADMIN_XHR_TEST")
                        .entityType("SYSTEM")
                        .performedBy("admin@example.com")
                        .status("SUCCESS")
                        .timestamp(LocalDateTime.now())
                        .build();
        publishUntilReceived(auditLog, messageReceived);

        assertEquals(0, messageReceived.getCount());
    }

    private CompletableFuture<StompSession> connect(
            WebSocketStompClient client, String origin, String token) {
        return connect(client, origin, token, new TestSessionHandler());
    }

    private CompletableFuture<StompSession> connect(
            WebSocketStompClient client,
            String origin,
            String token,
            TestSessionHandler sessionHandler) {
        WebSocketHttpHeaders handshakeHeaders = new WebSocketHttpHeaders();
        handshakeHeaders.setOrigin(origin);
        StompHeaders connectHeaders = new StompHeaders();
        if (token != null) {
            connectHeaders.add("Authorization", "Bearer " + token);
        }
        return client.connectAsync(
                "http://localhost:" + port + "/ws",
                handshakeHeaders,
                connectHeaders,
                sessionHandler);
    }

    private WebSocketStompClient webSocketTransportClient() {
        return client(List.of(new WebSocketTransport(new StandardWebSocketClient())));
    }

    private WebSocketStompClient xhrTransportClient() {
        return client(List.of(new RestTemplateXhrTransport()));
    }

    private WebSocketStompClient client(List<Transport> transports) {
        SockJsClient sockJsClient = new SockJsClient(transports);
        WebSocketStompClient client = new WebSocketStompClient(sockJsClient);
        client.setMessageConverter(new JacksonJsonMessageConverter());
        client.start();
        clients.add(client);
        return client;
    }

    private void publishUntilReceived(AuditLogEntity auditLog, CountDownLatch messageReceived)
            throws InterruptedException {
        for (int attempt = 0; attempt < 20 && messageReceived.getCount() > 0; attempt++) {
            adminActivityNotifier.notifyAdmins(auditLog);
            messageReceived.await(100, TimeUnit.MILLISECONDS);
        }
    }

    private static final class TestSessionHandler extends StompSessionHandlerAdapter {
        private final CompletableFuture<Throwable> failure = new CompletableFuture<>();

        @Override
        public void handleException(
                StompSession session,
                StompCommand command,
                StompHeaders headers,
                byte[] payload,
                Throwable exception) {
            failure.complete(exception);
        }

        @Override
        public void handleTransportError(StompSession session, Throwable exception) {
            failure.complete(exception);
        }

        CompletableFuture<Throwable> failure() {
            return failure;
        }
    }

    private static final class AdminActivityFrameHandler implements StompFrameHandler {
        private final CountDownLatch received;
        private final BlockingQueue<AdminActivityEvent> events = new LinkedBlockingQueue<>();
        private volatile AdminActivityEvent payload;

        private AdminActivityFrameHandler(CountDownLatch received) {
            this.received = received;
        }

        @Override
        public Type getPayloadType(StompHeaders headers) {
            return AdminActivityEvent.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            this.payload = (AdminActivityEvent) payload;
            events.offer(this.payload);
            received.countDown();
        }

        AdminActivityEvent payload() {
            return payload;
        }

        AdminActivityEvent next(long timeout, TimeUnit unit) throws InterruptedException {
            return events.poll(timeout, unit);
        }
    }
}
