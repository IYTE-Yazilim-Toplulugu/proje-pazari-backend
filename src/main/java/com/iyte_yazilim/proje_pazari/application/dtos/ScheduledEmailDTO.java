package com.iyte_yazilim.proje_pazari.application.dtos;

import java.time.LocalDateTime;

public record ScheduledEmailDTO(
        String id,
        String subject,
        String body,
        String targetRole,
        LocalDateTime scheduledAt,
        String status,
        String createdBy,
        LocalDateTime createdAt) {}
