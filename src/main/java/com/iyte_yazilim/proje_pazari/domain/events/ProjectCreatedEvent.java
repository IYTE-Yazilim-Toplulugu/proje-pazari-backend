package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;

public record ProjectCreatedEvent(
        String projectId,
        String projectTitle,
        String ownerId,
        String ownerEmail,
        String ownerName,
        LocalDateTime occurredOn)
        implements IDomainEvent {}
