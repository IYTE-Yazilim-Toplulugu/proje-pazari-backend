package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;
import java.util.List;

public record ProjectUpdatedEvent(
        String projectId,
        String projectTitle,
        String ownerId,
        String ownerEmail,
        String ownerName,
        List<String> teamMemberEmails,
        LocalDateTime occurredOn)
        implements IDomainEvent {}
