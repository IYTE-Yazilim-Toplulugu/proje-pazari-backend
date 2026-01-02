package com.iyte_yazilim.proje_pazari.application.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "User data transfer object")
public record UserDto(
        @Schema(description = "User ID", example = "01HQZX9K2M3N4P5Q6R7S8T9V0W") String userId,
        @Schema(description = "Email address", example = "user@example.com") String email,
        @Schema(description = "First name", example = "John") String firstName,
        @Schema(description = "Last name", example = "Doe") String lastName,
        @Schema(description = "User description/bio") String description,
        @Schema(description = "Profile picture URL") String profilePictureUrl,
        @Schema(description = "LinkedIn profile URL") String linkedinUrl,
        @Schema(description = "GitHub profile URL") String githubUrl,
        @Schema(description = "Preferred language (tr, en)", example = "en")
        String preferredLanguage) {}