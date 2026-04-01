package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.exceptions.ValidationException;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class UploadProfilePictureHandler
        implements IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>> {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<String> handle(UploadProfilePictureCommand command) {
        UserEntity user =
                userRepository
                        .findById(command.userId())
                        .orElseThrow(() -> new UserNotFoundException(command.userId()));

        if (command.file() == null || command.file().isEmpty()) {
            throw new ValidationException("File is required");
        }

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

        // Store avatar using organized bucket structure.
        String storedUrl = fileStorageService.storeUserAvatar(command.userId(), command.file());

        // Update user profile picture URL
        user.setProfilePictureUrl(storedUrl);
        userRepository.save(user);

        return ApiResponse.success(
                user.getProfilePictureUrl(),
                messageService.getMessage("user.profile.picture.uploaded"));
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

        // Handle presigned URL format and keep bucket + object path.
        // Example: http://minio:9000/bucket-name/users/user-1/avatar.jpg?...
        try {
            URI uri = URI.create(url.split("\\?")[0]);
            String path = uri.getPath();
            if (path != null && path.length() > 1) {
                return path.substring(1); // Remove leading slash only
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
