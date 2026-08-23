package com.iyte_yazilim.proje_pazari.application.service;

import com.iyte_yazilim.proje_pazari.application.dtos.AdminActivityEvent;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
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
            AdminActivityEvent event =
                    new AdminActivityEvent(
                            auditLog.getAction(),
                            auditLog.getEntityType(),
                            auditLog.getPerformedBy(),
                            auditLog.getStatus(),
                            auditLog.getTimestamp() != null
                                    ? auditLog.getTimestamp().toString()
                                    : null);

            messagingTemplate.convertAndSend("/topic/admin/activity", event);
            log.debug("Pushed audit event to WebSocket: {}", auditLog.getAction());
        } catch (Exception e) {
            log.warn("Failed to push audit event via WebSocket", e);
        }
    }
}
