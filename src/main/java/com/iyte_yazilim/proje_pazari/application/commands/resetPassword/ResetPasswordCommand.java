package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.validators.ValidPassword;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to reset a user's password using a reset token")
public record ResetPasswordCommand(
        @Schema(description = "Password reset token received by email")
                @NotBlank(message = "Reset token is required")
                String token,
        @Schema(description = "New password", example = "NewSecurePass1!")
                @NotBlank(message = "New password is required")
                @ValidPassword
                String newPassword)
        implements ICommand<ApiResponse<Void>> {}
