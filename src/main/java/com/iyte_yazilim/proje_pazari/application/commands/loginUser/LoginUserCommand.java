package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Command to login a user")
public record LoginUserCommand(
        @Schema(description = "Email address", example = "user@example.com")
                @NotBlank(message = "Email is required")
                @Email(message = "Email must be valid")
                String email,
        @Schema(description = "Password", example = "SecurePassword123!")
                @NotBlank(message = "Password is required")
                String password)
        implements IRequest<ApiResponse<LoginUserResult>> {}
