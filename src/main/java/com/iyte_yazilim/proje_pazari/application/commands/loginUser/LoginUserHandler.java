package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles the {@link LoginUserCommand} to authenticate users.
 *
 * <p>This handler orchestrates the login process:
 *
 * <ol>
 *   <li>Validate command using {@link LoginUserValidator}
 *   <li>Find user by email address
 *   <li>Check if account is active
 *   <li>Verify password using BCrypt
 *   <li>Generate JWT token
 *   <li>Return login result with token
 * </ol>
 *
 * <h2>Error Scenarios:</h2>
 *
 * <ul>
 *   <li>{@code BAD_REQUEST} - Validation failed
 *   <li>{@code BAD_REQUEST} - Invalid email or password
 *   <li>{@code BAD_REQUEST} - Account deactivated
 * </ul>
 *
 * <h2>Security Notes:</h2>
 *
 * <p>Error messages are intentionally vague ("Invalid email or password") to prevent user
 * enumeration attacks.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see LoginUserCommand
 * @see LoginUserResult
 * @see JwtUtil
 */
@Service
@RequiredArgsConstructor
public class LoginUserHandler
        implements IRequestHandler<LoginUserCommand, ApiResponse<LoginUserResult>> {

    private final UserRepository userRepository;
    private final IValidator<LoginUserCommand> validator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;

    /**
     * Handles user login command.
     *
     * <p>Authenticates the user and generates a JWT token for subsequent API access.
     *
     * @param command the login command containing credentials
     * @return API response with login result containing JWT token, or error message
     */
    @Override
    public ApiResponse<LoginUserResult> handle(LoginUserCommand command) {

        // --- 1. Validation ---
        var errors = validator.validate(command);
        if (errors != null && errors.length > 0) {
            String errorMessage = String.join(", ", errors);
            return ApiResponse.badRequest(errorMessage);
        }

        // --- 2. Find user by email ---
        UserEntity user = userRepository.findByEmail(command.email()).orElse(null);
        if (user == null) {
            return ApiResponse.badRequest(messageService.getMessage("auth.login.failed"));
        }

        // --- 3. Check if account is active ---
        if (user.getIsActive() == null || !user.getIsActive()) {
            return ApiResponse.badRequest(messageService.getMessage("auth.account.deactivated"));
        }

        // --- 4. Verify password with BCrypt ---
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            return ApiResponse.badRequest(messageService.getMessage("auth.login.failed"));
        }

        // --- 5. Generate JWT token with userId, email, and role ---
        String role = user.getRole() != null ? user.getRole().toString() : "USER";
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), role);

        // --- 6. Create result ---
        var result =
                new LoginUserResult(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        role,
                        token);

        // --- 7. Response with localized message ---
        return ApiResponse.success(result, messageService.getMessage("auth.login.success"));
    }
}
