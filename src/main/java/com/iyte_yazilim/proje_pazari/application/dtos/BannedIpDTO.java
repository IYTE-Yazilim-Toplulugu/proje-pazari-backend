package com.iyte_yazilim.proje_pazari.application.dtos;

import java.time.LocalDateTime;

public record BannedIpDTO(
        String id,
        String ipAddress,
        String reason,
        String bannedBy,
        LocalDateTime bannedAt,
        LocalDateTime expiresAt) {}
