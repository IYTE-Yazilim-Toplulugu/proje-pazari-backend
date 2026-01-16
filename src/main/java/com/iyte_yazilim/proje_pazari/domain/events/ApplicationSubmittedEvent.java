package com.iyte_yazilim.proje_pazari.domain.events;

public record ApplicationSubmittedEvent(
        String applicationId,
        String projectId,
        String projectTitle,
        String applicantId,
        String applicantEmail,
        String applicantFirstName,
        String ownerEmail,
        String ownerFirstName)
        implements IDomainEvent {}
