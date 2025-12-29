package com.iyte_yazilim.proje_pazari.domain.events;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ProjectStatusChangedEvent(
        String projectId,
        String projectTitle,
        String ownerId,
        String ownerEmail,
        String ownerName,
        ProjectStatus oldStatus,
        ProjectStatus newStatus,
        List<String> teamMemberEmails,
        LocalDateTime occurredOn
) implements IDomainEvent {}