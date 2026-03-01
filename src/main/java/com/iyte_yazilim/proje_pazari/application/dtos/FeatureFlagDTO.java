package com.iyte_yazilim.proje_pazari.application.dtos;

import java.time.LocalDateTime;

public record FeatureFlagDTO(
        String id,
        String flagKey,
        boolean enabled,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
