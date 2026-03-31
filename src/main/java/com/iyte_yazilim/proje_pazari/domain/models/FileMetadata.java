package com.iyte_yazilim.proje_pazari.domain.models;

import java.time.Instant;

public class FileMetadata {

    private String path;
    private long size;
    private String contentType;
    private Instant createdAt;
    private Instant lastModified;

    // Additional fields for extended metadata
    private String fileName;
    private String etag;

    public FileMetadata(
            String path,
            long size,
            String contentType,
            Instant createdAt,
            Instant lastModified,
            String fileName,
            String etag) {
        this.path = path;
        this.size = size;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.lastModified = lastModified;
        this.fileName = fileName;
        this.etag = etag;
    }

    public FileMetadata() {}

    public static FileMetadataBuilder builder() {
        return new FileMetadataBuilder();
    }

    public String getPath() {
        return this.path;
    }

    public long getSize() {
        return this.size;
    }

    public String getContentType() {
        return this.contentType;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getLastModified() {
        return this.lastModified;
    }

    public String getFileName() {
        return this.fileName;
    }

    public String getEtag() {
        return this.etag;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof FileMetadata)) return false;
        final FileMetadata other = (FileMetadata) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$path = this.getPath();
        final Object other$path = other.getPath();
        if (this$path == null ? other$path != null : !this$path.equals(other$path)) return false;
        if (this.getSize() != other.getSize()) return false;
        final Object this$contentType = this.getContentType();
        final Object other$contentType = other.getContentType();
        if (this$contentType == null
                ? other$contentType != null
                : !this$contentType.equals(other$contentType)) return false;
        final Object this$createdAt = this.getCreatedAt();
        final Object other$createdAt = other.getCreatedAt();
        if (this$createdAt == null
                ? other$createdAt != null
                : !this$createdAt.equals(other$createdAt)) return false;
        final Object this$lastModified = this.getLastModified();
        final Object other$lastModified = other.getLastModified();
        if (this$lastModified == null
                ? other$lastModified != null
                : !this$lastModified.equals(other$lastModified)) return false;
        final Object this$fileName = this.getFileName();
        final Object other$fileName = other.getFileName();
        if (this$fileName == null ? other$fileName != null : !this$fileName.equals(other$fileName))
            return false;
        final Object this$etag = this.getEtag();
        final Object other$etag = other.getEtag();
        if (this$etag == null ? other$etag != null : !this$etag.equals(other$etag)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof FileMetadata;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $path = this.getPath();
        result = result * PRIME + ($path == null ? 43 : $path.hashCode());
        final long $size = this.getSize();
        result = result * PRIME + (int) ($size >>> 32 ^ $size);
        final Object $contentType = this.getContentType();
        result = result * PRIME + ($contentType == null ? 43 : $contentType.hashCode());
        final Object $createdAt = this.getCreatedAt();
        result = result * PRIME + ($createdAt == null ? 43 : $createdAt.hashCode());
        final Object $lastModified = this.getLastModified();
        result = result * PRIME + ($lastModified == null ? 43 : $lastModified.hashCode());
        final Object $fileName = this.getFileName();
        result = result * PRIME + ($fileName == null ? 43 : $fileName.hashCode());
        final Object $etag = this.getEtag();
        result = result * PRIME + ($etag == null ? 43 : $etag.hashCode());
        return result;
    }

    public String toString() {
        return "FileMetadata(path="
                + this.getPath()
                + ", size="
                + this.getSize()
                + ", contentType="
                + this.getContentType()
                + ", createdAt="
                + this.getCreatedAt()
                + ", lastModified="
                + this.getLastModified()
                + ", fileName="
                + this.getFileName()
                + ", etag="
                + this.getEtag()
                + ")";
    }

    public static class FileMetadataBuilder {
        private String path;
        private long size;
        private String contentType;
        private Instant createdAt;
        private Instant lastModified;
        private String fileName;
        private String etag;

        FileMetadataBuilder() {}

        public FileMetadataBuilder path(String path) {
            this.path = path;
            return this;
        }

        public FileMetadataBuilder size(long size) {
            this.size = size;
            return this;
        }

        public FileMetadataBuilder contentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        public FileMetadataBuilder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public FileMetadataBuilder lastModified(Instant lastModified) {
            this.lastModified = lastModified;
            return this;
        }

        public FileMetadataBuilder fileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        public FileMetadataBuilder etag(String etag) {
            this.etag = etag;
            return this;
        }

        public FileMetadata build() {
            return new FileMetadata(
                    this.path,
                    this.size,
                    this.contentType,
                    this.createdAt,
                    this.lastModified,
                    this.fileName,
                    this.etag);
        }

        public String toString() {
            return "FileMetadata.FileMetadataBuilder(path="
                    + this.path
                    + ", size="
                    + this.size
                    + ", contentType="
                    + this.contentType
                    + ", createdAt="
                    + this.createdAt
                    + ", lastModified="
                    + this.lastModified
                    + ", fileName="
                    + this.fileName
                    + ", etag="
                    + this.etag
                    + ")";
        }
    }
}
