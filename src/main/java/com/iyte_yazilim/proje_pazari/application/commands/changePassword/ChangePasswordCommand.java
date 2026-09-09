package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.validators.PasswordMatches;
import com.iyte_yazilim.proje_pazari.application.validators.ValidPassword;
import com.iyte_yazilim.proje_pazari.domain.interfaces.PasswordMatchable;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to change user password")
@PasswordMatches
public record ChangePasswordCommand(
        @Schema(description = "User ID", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "User ID is required")
                String userId,
        @Schema(description = "Current password", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Current password is required")
                String currentPassword,
        @Schema(description = "New password", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "New password is required")
                @ValidPassword
                String newPassword,
        @Schema(description = "Confirm new password", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotBlank(message = "Password confirmation is required")
                String confirmPassword)
        implements ICommand<ApiResponse<Void>>, PasswordMatchable {}
