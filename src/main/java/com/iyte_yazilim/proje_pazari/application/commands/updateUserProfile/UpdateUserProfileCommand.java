package com.iyte_yazilim.proje_pazari.application.commands.updateUserProfile;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Command to update user profile")
public record UpdateUserProfileCommand(
        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED) String userId,
        @Schema(description = "First name")
                @Size(max = 50, message = "First name must not exceed 50 characters")
                String firstName,
        @Schema(description = "Last name")
                @Size(max = 50, message = "Last name must not exceed 50 characters")
                String lastName,
        @Schema(description = "User description/bio")
                @Size(max = 1000, message = "Description must not exceed 1000 characters")
                String description,
        @Schema(description = "LinkedIn profile URL") String linkedinUrl,
        @Schema(description = "GitHub profile URL") String githubUrl,
        @Schema(description = "Preferred language") String preferredLanguage)
        implements ICommand<ApiResponse<UserDto>> {}
