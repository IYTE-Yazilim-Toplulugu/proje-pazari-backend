package com.iyte_yazilim.proje_pazari.domain.models.results;

import java.time.LocalDateTime;

public record ApplicationSummaryResult(
        String applicationId,
        String applicantId,
        String applicantEmail,
        String applicantFirstName,
        String applicantLastName,
        String status,
        LocalDateTime createdAt) {}
