package com.iyte_yazilim.proje_pazari.domain.events;

import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;

public record ApplicationReviewedEvent(
        String applicationId,
        String applicantEmail,
        String projectTitle,
        ApplicationStatus status,
        String reviewMessage
) implements IDomainEvent {}
