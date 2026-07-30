package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.StoredFileNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Properties;
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

    /**
     * Suffix of the sidecar files that record the content type validated at upload time. Local disk
     * has no equivalent of MinIO's per-object metadata, so the value travels in a companion file.
     * The suffix is reserved: a path ending in it can neither be stored nor read back through this
     * adapter, so the sidecars are not themselves reachable from the public download endpoint.
     */
    private static final String METADATA_SUFFIX = ".meta";

    private static final String CONTENT_TYPE_KEY = "contentType";

    /** Lexical root: absolute and normalized, but symbolic links left unresolved. */
    private final Path storageLocation;

    /**
     * Same directory with every symbolic link resolved. Containment is checked against this, since
     * {@link Path#normalize()} is pure string math and would accept a link inside the storage
     * directory that points outside it. Held separately from {@link #storageLocation} because
     * callers' relative paths are resolved lexically (no filesystem access) before being checked.
     */
    private final Path storageRoot;

    public LocalStorageAdapter(@Value("${storage.local.path:./uploads}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        createStorageDirectory();
        this.storageRoot = resolveStorageRoot();
    }

    /**
     * Resolves the real path of the storage root once at startup. The root itself is frequently a
     * symbolic link (macOS {@code /tmp}, container mounts, JUnit temp dirs), so comparing candidate
     * real paths against an unresolved root would reject every legitimate file.
     */
    private Path resolveStorageRoot() {
        try {
            return storageLocation.toRealPath();
        } catch (IOException e) {
            throw new FileStorageException("Failed to resolve storage directory", e);
        }
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

            if (isMetadataPath(targetLocation)) {
                throw new FileStorageException("Invalid file path - reserved metadata suffix");
            }

            // Checked before createDirectories: if an existing ancestor is a link out of the
            // root, creating directories through it would materialize them outside the root.
            if (!isWithinStorageRoot(targetLocation)) {
                throw new FileStorageException("Invalid file path - resolves outside storage root");
            }

            // Create parent directories if they don't exist
            Files.createDirectories(targetLocation.getParent());

            Files.write(targetLocation, file.bytes());
            writeContentTypeSidecar(targetLocation, file.contentType());
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

            if (!filePath.startsWith(storageLocation) || isMetadataPath(filePath)) {
                throw new FileValidationException("Invalid file path - path traversal detected");
            }

            if (!isWithinStorageRoot(filePath)) {
                throw new FileValidationException(
                        "Invalid file path - resolves outside storage root");
            }

            Files.deleteIfExists(filePath);
            // Removed together with the file it describes, so a later upload to the same path
            // cannot inherit the previous file's recorded content type.
            Files.deleteIfExists(metadataPathFor(filePath));
            log.debug("Deleted local file: {}", path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file", e);
        }
    }

    @Override
    public boolean exists(String path) {
        Path filePath = storageLocation.resolve(path).normalize();
        return filePath.startsWith(storageLocation)
                && !isMetadataPath(filePath)
                && isWithinStorageRoot(filePath)
                && Files.exists(filePath);
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

        // Reported as missing rather than rejected: the sidecars are an implementation detail,
        // and a 404 does not disclose whether one exists.
        if (isMetadataPath(filePath)) {
            throw new StoredFileNotFoundException(path);
        }

        if (!isWithinStorageRoot(filePath)) {
            throw new FileValidationException("Invalid file path - resolves outside storage root");
        }

        if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
            throw new StoredFileNotFoundException(path);
        }

        return filePath;
    }

    /**
     * Reports whether {@code candidate} stays inside the storage root once symbolic links are
     * resolved. Checked against the deepest ancestor that currently exists, so the answer is
     * meaningful for a file that has not been written yet while still catching a linked ancestor.
     *
     * <p>Fails closed: an unreadable path is treated as outside the root rather than assumed safe.
     */
    private boolean isWithinStorageRoot(Path candidate) {
        Path existing = candidate;
        while (existing != null && !Files.exists(existing, LinkOption.NOFOLLOW_LINKS)) {
            existing = existing.getParent();
        }

        if (existing == null) {
            return false;
        }

        try {
            return existing.toRealPath().startsWith(storageRoot);
        } catch (IOException e) {
            log.warn("Failed to resolve real path for containment check: {}", e.getMessage());
            return false;
        }
    }

    private boolean isMetadataPath(Path filePath) {
        Path fileName = filePath.getFileName();
        return fileName != null && fileName.toString().endsWith(METADATA_SUFFIX);
    }

    private Path metadataPathFor(Path filePath) {
        return filePath.resolveSibling(filePath.getFileName().toString() + METADATA_SUFFIX);
    }

    /**
     * Returns the content type validated at upload time when a sidecar is present, falling back to
     * the filename extension for files written to the storage directory by other means (fixtures,
     * or uploads that predate the sidecars). Extension mapping is preferred over {@link
     * Files#probeContentType} because probing consults host-specific databases (e.g.
     * /etc/mime.types) and returns null on minimal containers, which would otherwise make the
     * served content type depend on where the app happens to run.
     *
     * <p>The result is descriptive only. Whether a type may be rendered inline is decided
     * separately at the HTTP boundary, so neither a stale sidecar nor a hostile filename here can
     * turn a stored file into active content.
     */
    private String resolveContentType(Path filePath) {
        String recorded = readContentTypeSidecar(filePath);
        if (recorded != null && !recorded.isBlank()) {
            return recorded;
        }

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

    private String readContentTypeSidecar(Path filePath) {
        Path sidecar = metadataPathFor(filePath);
        if (!Files.isRegularFile(sidecar)) {
            return null;
        }

        Properties recorded = new Properties();
        try (Reader reader = Files.newBufferedReader(sidecar, StandardCharsets.UTF_8)) {
            recorded.load(reader);
        } catch (IOException e) {
            log.warn("Failed to read stored content type for {}: {}", filePath, e.getMessage());
            return null;
        }
        return recorded.getProperty(CONTENT_TYPE_KEY);
    }

    /**
     * Records the validated content type alongside the stored file, or clears a stale record when
     * the upload carries no type, so a replacement never inherits the previous file's type.
     *
     * <p>A sidecar failure is logged rather than thrown: the file itself is already written, and
     * downloads fall back to the extension mapping — which, for uploads that came through {@code
     * FileStorageService}, is itself derived from the validated content type.
     */
    private void writeContentTypeSidecar(Path filePath, String contentType) {
        Path sidecar = metadataPathFor(filePath);
        try {
            if (contentType == null || contentType.isBlank()) {
                Files.deleteIfExists(sidecar);
                return;
            }

            Properties recorded = new Properties();
            recorded.setProperty(CONTENT_TYPE_KEY, contentType);
            try (Writer writer = Files.newBufferedWriter(sidecar, StandardCharsets.UTF_8)) {
                recorded.store(writer, "Content type validated at upload time");
            }
        } catch (IOException e) {
            log.warn("Failed to record content type for {}: {}", filePath, e.getMessage());
        }
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
