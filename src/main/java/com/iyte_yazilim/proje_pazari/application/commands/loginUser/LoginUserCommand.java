package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Command to authenticate a user and obtain a JWT token.
 *
 * <p>This command initiates the login process which includes:
 *
 * <ul>
 *   <li>Email and password validation
 *   <li>Account existence check
 *   <li>Password verification
 *   <li>JWT token generation
 * </ul>
 *
 * <h2>Example:</h2>
 *
 * <pre>{@code
 * LoginUserCommand command = new LoginUserCommand(
 *                 "student@std.iyte.edu.tr",
 *                 "SecurePassword123!");
 * ApiResponse<LoginUserResult> response = handler.handle(command);
 * String token = response.getData().token();
 * }</pre>
 *
 * @param email user's registered email address
 * @param password user's password
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see LoginUserHandler
 * @see com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult
 */
@Schema(description = "Command to login a user")
public record LoginUserCommand(
        @Schema(description = "Email address", example = "user@example.com")
                @NotBlank(message = "Email is required")
                @Email(message = "Email must be valid")
                String email,
        @Schema(description = "Password", example = "SecurePassword123!")
                @NotBlank(message = "Password is required")
                String password) {}
