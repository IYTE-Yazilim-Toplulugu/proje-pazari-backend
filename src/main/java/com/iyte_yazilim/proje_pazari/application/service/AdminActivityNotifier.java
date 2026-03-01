package com.iyte_yazilim.proje_pazari.application.service;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Pushes audit log events to connected admin clients via WebSocket STOMP. Events are sent to
 * /topic/admin/activity.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminActivityNotifier {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Notify connected admin clients about a new audit log event.
     *
     * @param auditLog the audit log entry to broadcast
     */
    public void notifyAdmins(AuditLogEntity auditLog) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("action", auditLog.getAction());
            event.put("entityType", auditLog.getEntityType());
            event.put("performedBy", auditLog.getPerformedBy());
            event.put("status", auditLog.getStatus());
            event.put("details", auditLog.getDetails());
            event.put(
                    "timestamp",
                    auditLog.getTimestamp() != null ? auditLog.getTimestamp().toString() : null);

            messagingTemplate.convertAndSend("/topic/admin/activity", (Object) event);
            log.debug("Pushed audit event to WebSocket: {}", auditLog.getAction());
        } catch (Exception e) {
            log.warn("Failed to push audit event via WebSocket", e);
        }
    }
}
