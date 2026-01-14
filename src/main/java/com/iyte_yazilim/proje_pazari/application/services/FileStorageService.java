package com.iyte_yazilim.proje_pazari.application.services;

import com.github.f4b6a3.ulid.UlidCreator;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IFileStorageAdapter;
import com.iyte_yazilim.proje_pazari.domain.models.FileMetadata;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final IFileStorageAdapter storageAdapter;

    @Value("${storage.max-file-size:10MB}")
    private DataSize maxFileSize;

    @Value(
            "${storage.allowed-content-types:image/jpeg,image/png,image/gif,image/webp,application/pdf}")
    private String allowedContentTypesString;

    /**
     * Stores a file in cloud storage.
     *
     * @param file the file to store
     * @param directory the storage directory (e.g., "profiles", "projects")
     * @return the file URL
     */
    public String storeFile(MultipartFile file, String directory) {
        // Validate file type
        validateFile(file);

        String fileName = generateUniqueFileName(file.getOriginalFilename());
        String path = directory + "/" + fileName;

        return storageAdapter.store(file, path);
    }

    public String getFileUrl(String filePath, int expirationMinutes) {
        return storageAdapter.generatePresignedUrl(filePath, expirationMinutes);
    }

    public String getFileUrl(String filePath) {
        return storageAdapter.generatePresignedUrl(filePath, 60);
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
     * Generates a unique file name.
     *
     * @param originalFilename the original file name
     * @return the unique file name
     */
    private String generateUniqueFileName(String originalFilename) {
        String extension = getFileExtension(originalFilename);
        String ulid = UlidCreator.getUlid().toString();
        return ulid + extension;
    }

    private String getFileExtension(String filename) {
        if (filename.contains(".")) {
            return filename.substring(filename.lastIndexOf("."));
        }
        return "";
    }

    private void validatePath(String path) {
        if (path == null
                || path.isBlank()
                || path.contains("..")
                || path.startsWith("/")
                || path.startsWith("\\")) {
            throw new FileStorageException("Invalid file path");
        }
    }

    /**
     * Validates a file.
     *
     * @param file the file to validate
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileStorageException("File is empty");
        }

        if (file.getSize() > maxFileSize.toBytes()) {
            throw new FileStorageException(
                    String.format("File size exceeds the maximum allowed size of %s", maxFileSize));
        }

        String contentType = file.getContentType();
        List<String> allowedContentTypes = getAllowedContentTypes();
        if (contentType == null || !allowedContentTypes.contains(contentType)) {
            throw new FileStorageException(
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
