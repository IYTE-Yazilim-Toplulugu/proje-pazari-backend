package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;

import java.util.List;

/**
 * Interface for file storage operations. Implementations can use Azure Blob, AWS S3, MinIO, etc.
 */
public interface IFileStorageAdapter {

    /**
     * Stores a file in the storage.
     *
     * @param file the file to store
     * @param path the storage path (e.g., "profiles/user123.jpg")
     * @return the storage URL or identifier
     */
    String store(FileUpload file, String path);

    /**
     * Generates a pre-signed URL for secure file access.
     *
     * @param path the file path
     * @param expirationMinutes URL validity duration
     * @return pre-signed URL
     */
    String generatePresignedUrl(String path, int expirationMinutes);

    /**
     * Deletes a file from storage.
     *
     * @param path the file path
     */
    void delete(String path);

    /**
     * Checks if a file exists.
     *
     * @param path the file path
     * @return true if file exists
     */
    boolean exists(String path);

    /**
     * Gets file metadata.
     *
     * @param path the file path
     * @return file metadata
     */
    FileMetadata getMetadata(String path);

    /**
     * Resolves how a file should be delivered to the client for download.
     *
     * <p>The default implementation preserves existing behavior for adapters that expose a
     * pre-signed/public URL: it wraps {@link #generatePresignedUrl(String, int)} in a {@link
     * StorageDownloadResult.RedirectResult}. Adapters that cannot produce an externally
     * redirectable URL (e.g. local filesystem storage) must override this method and return a
     * {@link StorageDownloadResult.InlineResult} instead, to avoid the caller redirecting to a
     * URL that points back at itself.
     *
     * @param path the file path
     * @param expirationMinutes URL validity duration, relevant only for redirect-capable adapters
     * @return a {@link StorageDownloadResult} describing how to deliver the file
     */
    default StorageDownloadResult resolveDownload(String path, int expirationMinutes) {
        return new StorageDownloadResult.RedirectResult(generatePresignedUrl(path, expirationMinutes));
    }

    /** Checks whether the storage backend is reachable and operational. */
    default boolean isAvailable() {
        return true;
    }

    /** Returns total storage capacity in bytes if available, otherwise null. */
    default Long getTotalSpaceBytes() {
        return null;
    }

    /** Returns currently used storage size in bytes if available, otherwise null. */
    default Long getUsedSpaceBytes() {
        return null;
    }

    /** Returns available logical buckets/containers for the storage backend. */
    default List<String> listBuckets() {
        return List.of();
    }
}
