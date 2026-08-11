package com.iyte_yazilim.proje_pazari.application.services;

import com.github.f4b6a3.ulid.UlidCreator;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import com.iyte_yazilim.proje_pazari.domain.models.FileUpload;
import com.iyte_yazilim.proje_pazari.domain.models.StorageDownloadResult;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    /**
     * Canonical extension for each content type the upload boundary accepts.
     *
     * <p>The stored extension is derived from the validated content type instead of being kept from
     * the client-supplied filename. Only the declared multipart content type is validated, so a
     * filename is free to disagree with it: an upload named "avatar.html" declaring "image/jpeg"
     * would otherwise be stored with its active extension intact and handed back by the public
     * download endpoint as HTML.
     */
    private static final Map<String, String> CANONICAL_EXTENSIONS =
            Map.of(
                    "image/jpeg", ".jpg",
                    "image/png", ".png",
                    "image/gif", ".gif",
                    "image/webp", ".webp",
                    "application/pdf", ".pdf");

    /**
     * Extension for an allowed content type with no canonical mapping above, which is reachable by
     * widening {@code storage.allowed-content-types}. Deliberately opaque rather than rejected, so
     * extending the configuration does not start failing uploads; the real content type is still
     * recorded by the storage adapter.
     */
    private static final String OPAQUE_EXTENSION = ".bin";

    private final IFileStorageAdapter storageAdapter;

    @Value("${storage.max-file-size:10MB}")
    private DataSize maxFileSize;

    @Value(
            "${storage.allowed-content-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String allowedContentTypesString;

    @Value("${storage.buckets.avatars:proje-pazari-avatars}")
    private String avatarsBucket;

    @Value("${storage.buckets.documents:proje-pazari-documents}")
    private String documentsBucket;

    /**
     * Stores a file in cloud storage.
     *
     * @param file the file to store
     * @param directory the storage directory (e.g., "profiles", "projects")
     * @return the file URL
     */
    public String storeFile(MultipartFile file, String directory) {
        validateFile(file);

        String fileName = generateUniqueFileName(file);
        String path = directory + "/" + fileName;

        return storageAdapter.store(toFileUpload(file), path);
    }

    /**
     * Stores a user avatar using organized bucket structure.
     *
     * <p>Storage path format: {avatarsBucket}/users/{userId}/avatar.{ext}
     */
    public String storeUserAvatar(String userId, MultipartFile file) {
        validateFile(file);
        validateStorageKeyPart(userId, "userId");

        String extension = storedExtension(file);
        String objectName = "users/" + userId + "/avatar" + extension;
        return storageAdapter.store(toFileUpload(file), avatarsBucket + "/" + objectName);
    }

    /**
     * Stores a project document using organized bucket structure.
     *
     * <p>Storage path format: {documentsBucket}/projects/{projectId}/{documentId}.{ext}
     */
    public String storeProjectDocument(String projectId, String documentId, MultipartFile file) {
        validateFile(file);
        validateStorageKeyPart(projectId, "projectId");
        validateStorageKeyPart(documentId, "documentId");

        String extension = storedExtension(file);
        String objectName = "projects/" + projectId + "/" + documentId + extension;
        return storageAdapter.store(toFileUpload(file), documentsBucket + "/" + objectName);
    }

    public String getFileUrl(String filePath, int expirationMinutes) {
        return storageAdapter.generatePresignedUrl(filePath, expirationMinutes);
    }

    public String getFileUrl(String filePath) {
        return storageAdapter.generatePresignedUrl(filePath, 60);
    }

    /**
     * Resolves how a file at {@code path} should be delivered to a client. Delegates to the
     * configured {@link IFileStorageAdapter}, which decides between a redirect (e.g. MinIO/S3
     * presigned URL) or inline content (e.g. local disk) — see {@link StorageDownloadResult}.
     * Reuses the same path validation as {@link #deleteFile(String)} / {@link
     * #getFileMetadata(String)}, so traversal and malformed paths are rejected identically
     * regardless of the download strategy the adapter chooses.
     *
     * @param path the file path
     * @param expirationMinutes URL validity duration, used only when the adapter redirects
     * @return the resolved download result
     */
    public StorageDownloadResult getDownloadResult(String path, int expirationMinutes) {
        validatePath(path);
        return storageAdapter.resolveDownload(path, expirationMinutes);
    }

    /**
     * Deletes a file from storage.
     *
     * @param path the file path
     */
    public void deleteFile(String path) {
        validatePath(path);
        storageAdapter.delete(path);
    }

    /**
     * Checks if a file exists.
     *
     * @param path the file path
     * @return true if file exists
     */
    public boolean fileExists(String path) {
        validatePath(path);
        return storageAdapter.exists(path);
    }

    /**
     * Gets file metadata.
     *
     * @param path the file path
     * @return file metadata
     */
    public FileMetadata getFileMetadata(String path) {
        validatePath(path);
        return storageAdapter.getMetadata(path);
    }

    /**
     * Converts a Spring MultipartFile to a domain FileUpload value object. This is the boundary
     * point where the Spring web type is translated into a framework-independent domain type.
     */
    private FileUpload toFileUpload(MultipartFile file) {
        try {
            return new FileUpload(
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getBytes(),
                    file.getSize());
        } catch (java.io.IOException e) {
            throw new FileStorageException("Failed to read file content", e);
        }
    }

    /**
     * Generates a unique file name, carrying the extension that matches the file's validated
     * content type.
     *
     * @param file the file being stored, already validated
     * @return the unique file name
     */
    private String generateUniqueFileName(MultipartFile file) {
        String ulid = UlidCreator.getUlid().toString();
        return ulid + storedExtension(file);
    }

    /**
     * Maps the file's validated content type to the extension it will be stored under. Must be
     * called only after {@link #validateFile(MultipartFile)}, which is what establishes that the
     * content type is one the application accepts.
     */
    private String storedExtension(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return OPAQUE_EXTENSION;
        }

        // Parameters (e.g. "image/jpeg;charset=binary") are not part of the type identity.
        String bareType = contentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        return CANONICAL_EXTENSIONS.getOrDefault(bareType, OPAQUE_EXTENSION);
    }

    private void validateStorageKeyPart(String value, String fieldName) {
        if (value == null
                || value.isBlank()
                || value.contains("/")
                || value.contains("\\")
                || value.contains("..")) {
            throw new FileValidationException("Invalid " + fieldName);
        }
    }

    private void validatePath(String path) {
        if (path == null
                || path.isBlank()
                || path.contains("..")
                || path.startsWith("/")
                || path.startsWith("\\")) {
            throw new FileValidationException("Invalid file path");
        }
    }

    /**
     * Validates a file.
     *
     * @param file the file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileValidationException("File is empty");
        }
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new FileValidationException(
                    String.format("File size exceeds the maximum allowed size of %s", maxFileSize));
        }
        String contentType = file.getContentType();
        List<String> allowedContentTypes = getAllowedContentTypes();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new FileValidationException(
                    "File type not allowed. Allowed types: " + allowedContentTypes);
        }
    }

    /**
     * Parses the allowed content types from configuration.
     *
     * @return list of allowed content types
     */
    private List<String> getAllowedContentTypes() {
        return Arrays.stream(allowedContentTypesString.split(",")).map(String::trim).toList();
    }
}
