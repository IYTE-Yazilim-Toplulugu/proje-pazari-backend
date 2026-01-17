package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Command to change user password")
public record ChangePasswordCommand(
        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "User ID is required")
                String userId,
        @Schema(description = "Current password", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Current password is required")
                String currentPassword,
        @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "New password is required")
                @Size(min = 8, message = "New password must be at least 8 characters")
                String newPassword,
        @Schema(description = "Confirm new password", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Password confirmation is required")
                String confirmPassword)
        implements IRequest {

    public void validate() {
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }
        if (currentPassword.equals(newPassword)) {
            throw new IllegalArgumentException(
                    "New password must be different from current password");
        }
    }
}
