package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Command to reset a user's password using a reset token")
public record ResetPasswordCommand(
        @Schema(description = "Password reset token received by email")
                @NotBlank(message = "Reset token is required")
                String token,
        @Schema(description = "New password", example = "NewSecurePass1!")
                @NotBlank(message = "New password is required")
                @Size(min = 8, message = "Password must be at least 8 characters")
                String newPassword,
        @Schema(description = "New password confirmation")
                @NotBlank(message = "Password confirmation is required")
                String confirmPassword)
        implements ICommand<ApiResponse<Void>> {}
