package com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to deactivate user account")
public record DeactivateAccountCommand(
        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "User ID is required")
                String userId,
        @Schema(description = "Reason for deactivation") String reason) {}
