package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

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
    public String store(MultipartFile file, String path) {
        try {
            Path targetLocation = storageLocation.resolve(path).normalize();

            // Security: Verify path is within storage location
            if (!targetLocation.startsWith(storageLocation)) {
                throw new FileStorageException("Invalid file path - path traversal detected");
            }

            // Create parent directories if they don't exist
            Files.createDirectories(targetLocation.getParent());

            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
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
                throw new FileStorageException("Invalid file path - path traversal detected");
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
                throw new FileStorageException("Invalid file path - path traversal detected");
            }

            if (!Files.exists(filePath)) {
                throw new FileStorageException("File not found: " + path);
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
                throw new FileStorageException("Invalid file path - path traversal detected");
            }

            if (!Files.exists(filePath)) {
                throw new FileStorageException("File not found: " + path);
            }

            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new FileStorageException("Failed to retrieve file", e);
        }
    }
}
