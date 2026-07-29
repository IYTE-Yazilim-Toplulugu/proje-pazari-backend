package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.StoredFileNotFoundException;
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
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeType;

/**
 * Local file system storage implementation for development without external storage. Active when
 * storage.provider=local
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalStorageAdapter implements IFileStorageAdapter {

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

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
        Path filePath = resolveExistingFile(path);
        try {
            BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);

            return new FileMetadata(
                    path,
                    attrs.size(),
                    resolveContentType(filePath),
                    attrs.creationTime().toInstant(),
                    attrs.lastModifiedTime().toInstant(),
                    filePath.getFileName().toString(),
                    null);
        } catch (IOException e) {
            throw new FileStorageException("Failed to get file metadata", e);
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
        Path filePath = resolveExistingFile(path);

        try {
            return new StorageDownloadResult.InlineResult(
                    Files.readAllBytes(filePath),
                    resolveContentType(filePath),
                    filePath.getFileName().toString());
        } catch (IOException e) {
            throw new FileStorageException("Failed to read local file", e);
        }
    }

    /**
     * Resolves a caller-supplied relative path to a real file inside the storage root, applying the
     * single traversal guard shared by every read operation.
     */
    private Path resolveExistingFile(String path) {
        Path filePath = storageLocation.resolve(path).normalize();

        if (!filePath.startsWith(storageLocation)) {
            throw new FileValidationException("Invalid file path - path traversal detected");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new StoredFileNotFoundException(path);
        }

        return filePath;
    }

    /**
     * Determines the content type from the filename extension first, falling back to the platform's
     * probe. Extension mapping is used in preference because {@link Files#probeContentType}
     * consults host-specific databases (e.g. /etc/mime.types) and returns null on minimal
     * containers, which would otherwise make the served content type depend on where the app
     * happens to run.
     */
    private String resolveContentType(Path filePath) {
        String fileName = filePath.getFileName().toString();
        return MediaTypeFactory.getMediaType(fileName)
                .map(MimeType::toString)
                .orElseGet(
                        () -> {
                            try {
                                String probed = Files.probeContentType(filePath);
                                return probed != null && !probed.isBlank()
                                        ? probed
                                        : DEFAULT_CONTENT_TYPE;
                            } catch (IOException e) {
                                return DEFAULT_CONTENT_TYPE;
                            }
                        });
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
