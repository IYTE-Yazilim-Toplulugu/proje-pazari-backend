package com.iyte_yazilim.proje_pazari.application.commands.uploadProfilePicture;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Command to upload user profile picture")
public record UploadProfilePictureCommand(
        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "User ID is required")
                String userId,
        @Schema(description = "Profile picture file", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "File is required")
                MultipartFile file) {}
