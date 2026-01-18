package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadProfilePictureHandler
        implements IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>> {

    private static final String PROFILES_FOLDER = "profiles";

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final MessageService messageService; // EKLENMELI

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<String> handle(UploadProfilePictureCommand command) {
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound(messageService.getMessage("user.not.found"));
        }

        try {
            // Delete old profile picture if exists
            String oldUrl = user.getProfilePictureUrl();
            if (oldUrl != null && !oldUrl.isBlank()) {
                String oldPath = extractPathFromUrl(oldUrl);
                if (oldPath != null) {
                    try {
                        fileStorageService.deleteFile(oldPath);
                    } catch (FileStorageException e) {
                        // Ignore if old file doesn't exist
                    }
                }
            }

            // Store new file in profiles folder - returns presigned URL for MinIO
            String storedUrl = fileStorageService.storeFile(command.file(), PROFILES_FOLDER);

            // Update user profile picture URL
            user.setProfilePictureUrl(storedUrl);
            userRepository.save(user);

            return ApiResponse.success(
                    user.getProfilePictureUrl(),
                    messageService.getMessage("user.profile.picture.uploaded"));

        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (FileStorageException e) {
            return ApiResponse.error(
                    messageService.getMessage("file.upload.failed", new Object[] {e.getMessage()}));
        }
    }

    private String extractPathFromUrl(String url) {
        if (url == null) {
            return null;
        }

        // Handle API path format: /api/v1/files/profiles/filename.jpg
        if (url.contains("/api/v1/files/")) {
            return url.substring(url.indexOf("/api/v1/files/") + "/api/v1/files/".length());
        }

        // Handle simple storage path format (e.g., "profiles/filename.jpg")
        // This is the path returned by the storage adapter's store() method
        if (!url.startsWith("http") && !url.startsWith("/api")) {
            return url;
        }

        // Handle presigned URL format: extract bucket-relative path from URL
        // Example: http://minio:9000/bucket/profiles/filename.jpg?...
        // Extract everything after the bucket name (third path segment in URL)
        try {
            java.net.URI uri = java.net.URI.create(url.split("\\?")[0]);
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                // Remove leading slash and bucket name (first segment)
                String[] segments = path.substring(1).split("/", 2);
                if (segments.length > 1) {
                    return segments[1]; // Return path after bucket name
                }
            }
        } catch (IllegalArgumentException e) {
            // Fall back to original behavior if URL parsing fails
        }

        // Legacy fallback for /profiles/ pattern
        if (url.contains("/profiles/")) {
            int profilesIndex = url.indexOf("/profiles/");
            int queryIndex = url.indexOf("?");
            if (queryIndex > profilesIndex) {
                return url.substring(profilesIndex + 1, queryIndex);
            }
            return url.substring(profilesIndex + 1);
        }

        return null;
    }
}
