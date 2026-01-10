package com.iyte_yazilim.proje_pazari.domain.models;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMetadata {

    private String path;
    private long size;
    private String contentType;
    private Instant createdAt;
    private Instant lastModified;

    // Additional fields for extended metadata
    private String fileName;
    private String etag;
}
