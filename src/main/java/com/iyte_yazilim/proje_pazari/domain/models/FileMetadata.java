package com.iyte_yazilim.proje_pazari.domain.models;

import java.time.Instant;

public record FileMetadata(
        String path,
        long size,
        String contentType,
        Instant createdAt,
        Instant lastModified,

        // Additional fields for extended FileMetadata
        String fileName,
        String etag) {
    // You only need to write this if you want to enforce DDD validation rules
    public FileMetadata {
        if (path == null || path.isBlank()) throw new IllegalArgumentException("Path required");
        if (size < 0) throw new IllegalArgumentException("Size cannot be negative");
    }
}
