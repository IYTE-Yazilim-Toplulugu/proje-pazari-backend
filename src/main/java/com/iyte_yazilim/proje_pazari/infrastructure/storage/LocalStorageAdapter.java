package com.iyte_yazilim.proje_pazari.infrastructure.storage;

import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.StoredFileNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import com.iyte_yazilim.proje_pazari.infrastructure.storage.ContainedFileTree.ContainmentException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
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
 *
 * <p>Caller-supplied paths are validated lexically here — traversal out of the root and the
 * reserved sidecar suffix are rejected before anything touches the disk — while every actual read
 * and write goes through {@link ContainedFileTree}, which binds containment to the open itself
 * rather than to a pathname that could change afterwards. See that class for the guarantee's
 * boundaries.
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

    private final ContainedFileTree tree;

    public LocalStorageAdapter(@Value("${storage.local.path:./uploads}") String storagePath) {
        this.storageLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        createStorageDirectory();
        this.tree = openStorageTree();
    }

    private void createStorageDirectory() {
        try {
            Files.createDirectories(storageLocation);
            log.info("Initialized local file storage at: {}", storageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Failed to create storage directory", e);
        }
    }

    private ContainedFileTree openStorageTree() {
        try {
            return new ContainedFileTree(storageLocation);
        } catch (IOException e) {
            throw new FileStorageException("Failed to resolve storage directory", e);
        }
    }

    @Override
    public String store(FileUpload file, String path) {
        Path relative = relativePathOf(path);

        if (relative == null) {
            throw new FileStorageException("Invalid file path - path traversal detected");
        }
        if (isMetadataPath(relative)) {
            throw new FileStorageException("Invalid file path - reserved metadata suffix");
        }

        try {
            tree.write(relative, file.bytes());
        } catch (ContainmentException e) {
            throw new FileStorageException("Invalid file path - resolves outside storage root");
        } catch (IOException e) {
            throw new FileStorageException("Failed to store file locally", e);
        }

        writeContentTypeSidecar(relative, file.contentType());
        log.debug("Stored file locally: {}", path);

        // Return API path for local storage
        return "/api/v1/files/" + path;
    }

    @Override
    public String generatePresignedUrl(String path, int expirationMinutes) {
        // Local storage uses API endpoint (no presigned URLs)
        return "/api/v1/files/" + path;
    }

    @Override
    public void delete(String path) {
        Path relative = relativePathOf(path);

        if (relative == null || isMetadataPath(relative)) {
            throw new FileValidationException("Invalid file path - path traversal detected");
        }

        try {
            tree.deleteIfExists(relative);
        } catch (ContainmentException e) {
            throw new FileValidationException("Invalid file path - resolves outside storage root");
        } catch (IOException e) {
            throw new FileStorageException("Failed to delete file", e);
        }

        // Removed together with the file it describes, so a later upload to the same path
        // cannot inherit the previous file's recorded content type.
        deleteContentTypeSidecar(relative);
        log.debug("Deleted local file: {}", path);
    }

    @Override
    public boolean exists(String path) {
        Path relative = relativePathOf(path);
        return relative != null && !isMetadataPath(relative) && tree.exists(relative);
    }

    @Override
    public FileMetadata getMetadata(String path) {
        Path relative = requireStorableFile(path);
        BasicFileAttributes attrs = readAttributes(relative, path);

        return new FileMetadata(
                path,
                attrs.size(),
                resolveContentType(relative),
                attrs.creationTime().toInstant(),
                attrs.lastModifiedTime().toInstant(),
                fileName(relative),
                null);
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
        Path relative = requireStorableFile(path);
        readAttributes(relative, path);

        try {
            return new StorageDownloadResult.InlineResult(
                    tree.readAllBytes(relative), resolveContentType(relative), fileName(relative));
        } catch (ContainmentException e) {
            throw new FileValidationException("Invalid file path - resolves outside storage root");
        } catch (NoSuchFileException e) {
            throw new StoredFileNotFoundException(path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to read local file", e);
        }
    }

    /**
     * Applies the lexical guard shared by every read operation: the path must stay under the
     * storage root and must not name a sidecar.
     */
    private Path requireStorableFile(String path) {
        Path relative = relativePathOf(path);

        if (relative == null) {
            throw new FileValidationException("Invalid file path - path traversal detected");
        }

        // Reported as missing rather than rejected: the sidecars are an implementation detail,
        // and a 404 does not disclose whether one exists.
        if (isMetadataPath(relative)) {
            throw new StoredFileNotFoundException(path);
        }

        return relative;
    }

    private BasicFileAttributes readAttributes(Path relative, String path) {
        try {
            BasicFileAttributes attrs = tree.readAttributes(relative);
            if (!attrs.isRegularFile()) {
                throw new StoredFileNotFoundException(path);
            }
            return attrs;
        } catch (ContainmentException e) {
            throw new FileValidationException("Invalid file path - resolves outside storage root");
        } catch (NoSuchFileException e) {
            throw new StoredFileNotFoundException(path);
        } catch (IOException e) {
            throw new FileStorageException("Failed to get file metadata", e);
        }
    }

    /**
     * Resolves a caller-supplied path against the storage root and returns it relative to that
     * root, or {@code null} when it escapes lexically. Pure string math — the filesystem-level
     * guarantee is {@link ContainedFileTree}'s.
     */
    private Path relativePathOf(String path) {
        Path resolved = storageLocation.resolve(path).normalize();

        if (!resolved.startsWith(storageLocation) || resolved.equals(storageLocation)) {
            return null;
        }
        return storageLocation.relativize(resolved);
    }

    private boolean isMetadataPath(Path relative) {
        return fileName(relative).endsWith(METADATA_SUFFIX);
    }

    private Path sidecarOf(Path relative) {
        return relative.resolveSibling(fileName(relative) + METADATA_SUFFIX);
    }

    private String fileName(Path relative) {
        return relative.getFileName().toString();
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
    private String resolveContentType(Path relative) {
        String recorded = readContentTypeSidecar(relative);
        if (recorded != null && !recorded.isBlank()) {
            return recorded;
        }

        return MediaTypeFactory.getMediaType(fileName(relative))
                .map(MimeType::toString)
                .orElse(DEFAULT_CONTENT_TYPE);
    }

    private String readContentTypeSidecar(Path relative) {
        Properties recorded = new Properties();

        try (Reader reader =
                new InputStreamReader(
                        new ByteArrayInputStream(tree.readAllBytes(sidecarOf(relative))),
                        StandardCharsets.UTF_8)) {
            recorded.load(reader);
        } catch (NoSuchFileException e) {
            return null;
        } catch (IOException e) {
            // Includes a planted symbolic link at the sidecar path: not ours, so not read.
            log.warn("Failed to read stored content type for {}: {}", relative, e.getMessage());
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
    private void writeContentTypeSidecar(Path relative, String contentType) {
        if (contentType == null || contentType.isBlank()) {
            deleteContentTypeSidecar(relative);
            return;
        }

        Properties recorded = new Properties();
        recorded.setProperty(CONTENT_TYPE_KEY, contentType);

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            try (Writer writer = new OutputStreamWriter(buffer, StandardCharsets.UTF_8)) {
                recorded.store(writer, "Content type validated at upload time");
            }
            tree.write(sidecarOf(relative), buffer.toByteArray());
        } catch (IOException e) {
            log.warn("Failed to record content type for {}: {}", relative, e.getMessage());
        }
    }

    private void deleteContentTypeSidecar(Path relative) {
        try {
            tree.deleteIfExists(sidecarOf(relative));
        } catch (IOException e) {
            log.warn("Failed to remove stored content type for {}: {}", relative, e.getMessage());
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
