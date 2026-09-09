package com.iyte_yazilim.proje_pazari.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.application.dtos.AdminActivityEvent;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
class AdminActivityNotifierTest {

    @Mock private SimpMessagingTemplate messagingTemplate;

    @Test
    void broadcastsMinimalTypedPayloadWithoutArbitraryDetails() throws Exception {
        AdminActivityNotifier notifier = new AdminActivityNotifier(messagingTemplate);
        LocalDateTime timestamp = LocalDateTime.of(2026, 8, 23, 12, 30);
        AuditLogEntity auditLog =
                AuditLogEntity.builder()
                        .action("ADMIN_UPDATE_USER")
                        .entityType("USER")
                        .performedBy("admin@example.com")
                        .status("SUCCESS")
                        .timestamp(timestamp)
                        .details("sensitive arbitrary exception or request data")
                        .ipAddress("203.0.113.10")
                        .entityId("private-entity-id")
                        .build();

        notifier.notifyAdmins(auditLog);

        ArgumentCaptor<Object> payload = ArgumentCaptor.forClass(Object.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/admin/activity"), payload.capture());
        AdminActivityEvent event = (AdminActivityEvent) payload.getValue();
        assertEquals("ADMIN_UPDATE_USER", event.action());
        assertEquals("USER", event.entityType());
        assertEquals("admin@example.com", event.performedBy());
        assertEquals("SUCCESS", event.status());
        assertEquals(timestamp.toString(), event.timestamp());

        String json = new ObjectMapper().writeValueAsString(event);
        assertFalse(json.contains("details"));
        assertFalse(json.contains("ipAddress"));
        assertFalse(json.contains("entityId"));
        assertFalse(json.contains("sensitive arbitrary"));
    }
}
