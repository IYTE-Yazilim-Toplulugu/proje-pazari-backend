package com.iyte_yazilim.proje_pazari.application.dtos;

/**
 * Minimal live admin activity notification.
 *
 * <p>Arbitrary audit details, IP addresses, and entity identifiers are intentionally excluded from
 * the broker payload. Administrators can retrieve full audit records through the separately
 * authorized REST API when needed.
 */
public record AdminActivityEvent(
        String action, String entityType, String performedBy, String status, String timestamp) {}
