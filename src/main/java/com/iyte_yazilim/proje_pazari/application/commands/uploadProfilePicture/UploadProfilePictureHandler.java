package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadProfilePictureHandler
        implements IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>> {

    private static final String PROFILES_FOLDER = "profiles";

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ApiResponse<String> handle(UploadProfilePictureCommand command) {
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound("User not found");
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

            return ApiResponse.success(storedUrl, "Profile picture uploaded successfully");
        } catch (FileStorageException e) {
            return ApiResponse.error("Failed to upload file: " + e.getMessage());
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

        // Handle presigned URL format: extract path from URL
        // Example: http://minio:9000/bucket/profiles/filename.jpg?...
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
