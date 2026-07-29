package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Local file system storage implementation for development without external storage. Active when
 * storage.provider=local
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalStorageAdapter implements IFileStorageAdapter {

    private final Path storageLocation;

    public LocalStorageAdapter(@Value("${storage.local.path:./uploads}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        createStorageDirectory();
    }

    private void createStorageDirectory() {
        try {
            Files.createDirectories(storageLocation);
            log.info("Initialized local file storage at: {}", storageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Failed to create storage directory", e);
        }
    }

    @Override
    public String store(FileUpload file, String path) {
        try {
            Path targetLocation = storageLocation.resolve(path).normalize();

            // Security: Verify path is within storage location
            if (!targetLocation.startsWith(storageLocation)) {
                throw new FileStorageException("Invalid file path - path traversal detected");
            }

            // Create parent directories if they don't exist
            Files.createDirectories(targetLocation.getParent());

            Files.write(targetLocation, file.bytes());
            log.debug("Stored file locally: {}", path);

            // Return API path for local storage
            return "/api/v1/files/" + path;
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file locally", e);
        }
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        // Local storage uses API endpoint (no presigned URLs)
        return "/api/v1/files/" + path;
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = storageLocation.resolve(path).normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new FileValidationException("Invalid file path - path traversal detected");
            }

            Files.deleteIfExists(filePath);
            log.debug("Deleted local file: {}", path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file", e);
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = storageLocation.resolve(path).normalize();
        return filePath.startsWith(storageLocation) && Files.exists(filePath);
    }

    @Override
    public FileMetadata getMetadata(String path) {
        try {
            Path filePath = storageLocation.resolve(path).normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new FileValidationException("Invalid file path - path traversal detected");
            }

            if (!Files.exists(filePath)) {
                throw new FileValidationException("File not found: " + path);
            }

            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
            String contentType = Files.probeContentType(filePath);

            return new FileMetadata(
                    path,
                    attrs.size(),
                    contentType != null ? contentType : "application/octet-stream",
                    attrs.creationTime().toInstant(),
                    attrs.lastModifiedTime().toInstant(),
                    filePath.getFileName().toString(),
                    null);
        } catch (IOException e) {
            throw new FileStorageException("Failed to get file metadata", e);
        }
    }

    /** Retrieves file content as bytes. Used internally for serving files. */
    public byte[] retrieveAsBytes(String path) {
        try {
            Path filePath = storageLocation.resolve(path).normalize();

            if (!filePath.startsWith(storageLocation)) {
                throw new FileValidationException("Invalid file path - path traversal detected");
            }

            if (!Files.exists(filePath)) {
                throw new FileValidationException("File not found: " + path);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileValidationException("Failed to retrieve file", e);
        }
    }

    /**
     * Reads the file directly from disk and returns it as an {@link
     * StorageDownloadResult.InlineResult}, since local storage has no externally redirectable URL
     * to offer. This is the fix for the infinite-redirect bug: previously {@link
     * #generatePresignedUrl(String, int)} was reused for downloads and returned a URL pointing back
     * at this same API endpoint.
     */
    @Override
    public StorageDownloadResult resolveDownload(String path, int expirationMinutes) {
        Path filePath = storageLocation.resolve(path).normalize();

        // Security: same traversal guard used by store/delete/exists/getMetadata
        if (!filePath.startsWith(storageLocation)) {
            throw new FileValidationException("Invalid file path - path traversal detected");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new FileValidationException("File not found: " + path);
        }

        try {
            byte[] content = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }

            String filename = safeFilename(filePath.getFileName().toString());

            return new StorageDownloadResult.InlineResult(content, contentType, filename);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read local file", e);
        }
    }

    /** Strips characters that could enable HTTP header injection via Content-Disposition. */
    private String safeFilename(String rawName) {
        if (rawName == null || rawName.isBlank()) {
            return "download";
        }
        return rawName.replaceAll("[\\r\\n\"\\\\]", "_");
    }

    @Override
    public boolean isAvailable() {
        return Files.exists(storageLocation) && Files.isDirectory(storageLocation);
    }

    @Override
    public Long getTotalSpaceBytes() {
        return storageLocation.toFile().getTotalSpace();
    }

    @Override
    public Long getUsedSpaceBytes() {
        long total = storageLocation.toFile().getTotalSpace();
        long free = storageLocation.toFile().getUsableSpace();
        if (total <= 0L) {
            return null;
        }
        return Math.max(total - free, 0L);
    }

    @Override
    public List<String> listBuckets() {
        Path name = storageLocation.getFileName();
        if (name == null) {
            return List.of(storageLocation.toString());
        }
        return List.of(name.toString());
    }
}
