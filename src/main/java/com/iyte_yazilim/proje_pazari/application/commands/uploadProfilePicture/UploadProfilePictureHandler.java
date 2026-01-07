package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UploadProfilePictureHandler
        implements IRequestHandler<UploadProfilePictureCommand, ApiResponse<String>> {

    private final FileStorageService fileStorageService;
    private final UserRepository userRepository;
    private final MessageService messageService; // EKLENMELI

    @Override
    @Transactional
    public ApiResponse<String> handle(UploadProfilePictureCommand command) {
        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound(messageService.getMessage("user.not.found"));
        }

        try {
            // Delete old profile picture if exists
            if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isBlank()) {
                String oldFileName =
                        user.getProfilePictureUrl()
                                .substring(user.getProfilePictureUrl().lastIndexOf("/") + 1);
                // Validate extracted filename to prevent path traversal
                if (oldFileName != null
                        && !oldFileName.isBlank()
                        && !oldFileName.contains("..")
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

            return ApiResponse.success(
                    user.getProfilePictureUrl(), messageService.getMessage("file.upload.success"));
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        } catch (IOException e) {
            return ApiResponse.error(
                    messageService.getMessage("file.upload.failed", new Object[] {e.getMessage()}));
        }
    }
}