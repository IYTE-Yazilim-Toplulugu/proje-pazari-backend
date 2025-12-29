package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Command to register a new user in the system.
 *
 * <p>This command initiates the user registration process which includes:
 *
 * <ul>
 *   <li>Email validation
 *   <li>Password strength validation (min 8 characters)
 *   <li>Duplicate email check
 *   <li>Password encryption
 *   <li>User entity creation
 * </ul>
 *
 * <h2>Validation Rules:</h2>
 *
 * <table border="1">
 * <tr>
 * <th>Field</th>
 * <th>Rule</th>
 * <th>Example</th>
 * </tr>
 * <tr>
 * <td>email</td>
 * <td>Valid email format</td>
 * <td>student@example.com</td>
 * </tr>
 * <tr>
 * <td>password</td>
 * <td>Min 8 characters</td>
 * <td>SecurePass123!</td>
 * </tr>
 * <tr>
 * <td>firstName</td>
 * <td>2-50 characters</td>
 * <td>John</td>
 * </tr>
 * <tr>
 * <td>lastName</td>
 * <td>2-50 characters</td>
 * <td>Doe</td>
 * </tr>
 * </table>
 *
 * <h2>Example:</h2>
 *
 * <pre>{@code
 * RegisterUserCommand command = new RegisterUserCommand(
 *                 "student@std.iyte.edu.tr",
 *                 "SecurePassword123!",
 *                 "John",
 *                 "Doe");
 * ApiResponse<RegisterUserResult> response = handler.handle(command);
 * }</pre>
 *
 * @param email user's email address (required, unique)
 * @param password user's password (required, min 8 chars)
 * @param firstName user's first name (required, 2-50 chars)
 * @param lastName user's last name (required, 2-50 chars)
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see RegisterUserHandler
 * @see com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult
 */
@Schema(description = "Command to register a new user")
public record RegisterUserCommand(
        @Schema(description = "Email address", example = "user@example.com")
                @NotBlank(message = "Email is required")
                @Email(message = "Email must be valid")
                String email,
        @Schema(description = "Password", example = "SecurePassword123!")
                @NotBlank(message = "Password is required")
                @Size(min = 8, message = "Password must be at least 8 characters")
                @Pattern(
                        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
                        message = "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character")
                String password,
        @Schema(description = "First name", example = "John")
                @NotBlank(message = "First name is required")
                @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
                String firstName,
        @Schema(description = "Last name", example = "Doe")
                @NotBlank(message = "Last name is required")
                @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
                String lastName) {}
