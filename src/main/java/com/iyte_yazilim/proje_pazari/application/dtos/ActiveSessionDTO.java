package com.iyte_yazilim.proje_pazari.application.dtos;

import java.time.Instant;

public record ActiveSessionDTO(
        String userId, String email, long sessionCount, Instant lastActivity) {}
