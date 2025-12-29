package com.iyte_yazilim.proje_pazari.domain.events;

public record ApplicationSubmittedEvent(
        String applicationId,
        String projectId,
        String applicantEmail,
        String projectTitle
) implements IDomainEvent {}