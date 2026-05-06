package com.iyte_yazilim.proje_pazari.application.commands.forgotPassword;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to request a password reset email")
public record ForgotPasswordCommand(
        @Schema(description = "Email address of the account", example = "student@std.iyte.edu.tr")
                @NotBlank(message = "Email is required")
                @Email(message = "Invalid email format")
                String email)
        implements ICommand<ApiResponse<Void>> {}
