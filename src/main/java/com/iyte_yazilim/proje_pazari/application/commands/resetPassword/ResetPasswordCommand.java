package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.validators.PasswordMatches;
import com.iyte_yazilim.proje_pazari.application.validators.ValidPassword;
import com.iyte_yazilim.proje_pazari.domain.interfaces.PasswordMatchable;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to reset a user's password using a reset token")
@PasswordMatches
public record ResetPasswordCommand(
        @Schema(description = "Password reset token received by email")
                @NotBlank(message = "Reset token is required")
                String token,
        @Schema(description = "New password", example = "NewSecurePass1!")
                @NotBlank(message = "New password is required")
                @ValidPassword
                String newPassword,
        @Schema(description = "New password confirmation")
                @NotBlank(message = "Password confirmation is required")
                String confirmPassword)
        implements ICommand<ApiResponse<Void>>, PasswordMatchable {}
