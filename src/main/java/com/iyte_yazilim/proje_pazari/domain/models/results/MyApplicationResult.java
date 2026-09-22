package com.iyte_yazilim.proje_pazari.domain.models.results;

import java.time.LocalDateTime;

public record MyApplicationResult(
        String applicationId,
        String projectId,
        String projectTitle,
        String status,
        LocalDateTime createdAt) {}
