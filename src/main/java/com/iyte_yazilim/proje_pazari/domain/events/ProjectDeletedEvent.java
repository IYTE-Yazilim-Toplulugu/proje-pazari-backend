package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain event published when a project is deleted.
 *
 * <p>Contains information about the deleted project, its owner, the number of pending applications
 * that were rejected as a result of the deletion, and the contact details of affected applicants
 * for notification purposes.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.2
 * @since 2026-03-23
 */
public record ProjectDeletedEvent(
        String projectId,
        String projectTitle,
        String ownerId,
        String ownerEmail,
        String ownerName,
        int rejectedApplicationCount,
        List<String> applicantEmails,
        List<String> applicantNames,
        LocalDateTime occurredOn)
        implements IDomainEvent {}
