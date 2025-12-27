package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class UploadProfilePictureHandler implements IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>> {

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
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isBlank()) {
                String oldFileName = user.getProfilePictureUrl().substring(user.getProfilePictureUrl().lastIndexOf("/") + 1);
                // Validate extracted filename to prevent path traversal
                if (!oldFileName.contains("..") 
                        && !oldFileName.contains("/") 
                        && !oldFileName.contains("\\")) {
                    try {
                        fileStorageService.deleteFile(oldFileName);
                    } catch (IOException e) {
                        // Ignore if old file doesn't exist
                    }
                }
            }

            // Store new file
            String fileName = fileStorageService.storeFile(command.file(), command.userId());

            // Update user profile picture URL
            user.setProfilePictureUrl("/api/v1/files/" + fileName);
            userRepository.save(user);

            return ApiResponse.success(user.getProfilePictureUrl(), "Profile picture uploaded successfully");
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (IOException e) {
            return ApiResponse.error("Failed to upload file: " + e.getMessage());
        }
    }
}
