package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Command to register a new user")
public record RegisterUserCommand(

        @Schema(description = "Email address", example = "user@example.com") @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,

        @Schema(description = "Password", example = "SecurePassword123!") @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,

        @Schema(description = "First name", example = "John") @NotBlank(message = "First name is required") @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters") String firstName,

        @Schema(description = "Last name", example = "Doe") @NotBlank(message = "Last name is required") @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters") String lastName) {
}
