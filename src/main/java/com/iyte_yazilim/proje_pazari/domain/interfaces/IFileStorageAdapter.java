package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

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
    String store(MultipartFile file, String path);

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
