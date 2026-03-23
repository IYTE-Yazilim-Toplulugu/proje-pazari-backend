package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;

/**
 * Domain event published when a project is deleted.
 *
 * <p>Contains information about the deleted project, its owner, and the number of pending
 * applications that were rejected as a result of the deletion.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
 * @since 2026-03-23
 */
public record ProjectDeletedEvent(
        String projectId,
        String projectTitle,
        String ownerId,
        String ownerEmail,
        String ownerName,
        int rejectedApplicationCount,
        LocalDateTime occurredOn)
        implements IDomainEvent {}
