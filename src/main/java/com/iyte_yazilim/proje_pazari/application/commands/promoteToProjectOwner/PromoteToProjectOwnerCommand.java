package com.iyte_yazilim.proje_pazari.application.commands.promoteToProjectOwner;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to promote a user to PROJECT_OWNER role")
public record PromoteToProjectOwnerCommand(
        @Schema(description = "User ID to promote", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "User ID is required")
                String userId) {}
