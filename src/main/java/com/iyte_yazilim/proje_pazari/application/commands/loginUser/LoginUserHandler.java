package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailNotVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoginUserHandler
        implements IRequestHandler<LoginUserCommand, ApiResponse<LoginUserResult>> {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final IValidator<LoginUserCommand> validator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final MessageService messageService;
    private final RefreshTokenService refreshTokenService;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    /**
     * Handles user login command.
     *
     * <p>Authenticates the user and generates a JWT token for subsequent API access.
     *
     * @param command the login command containing credentials
     * @return API response with login result containing JWT token, or error message
     */
    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
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

        // --- 5. Check email verification ---
        boolean isVerified =
                emailVerificationRepository.existsByUserIdAndVerifiedAtIsNotNull(user.getId());
        if (!isVerified) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in. Check your inbox.");
        }

        // --- 6. Generate JWT token with userId, email, and role ---
        String role = user.getRole() != null ? user.getRole().toString() : "APPLICANT";
        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), role);

        // --- 7. Generate refresh token ---
        String refreshToken = refreshTokenService.createRefreshToken(user.getId());
        // --- 7. Create result ---
        var result =
                new LoginUserResult(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        role,
                        accessToken,
                        refreshToken,
                        jwtExpiration);

        // --- 8. Response with localized message ---
        return ApiResponse.success(result, messageService.getMessage("auth.login.success"));
    }
}
