package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailNotVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginUserHandler
        implements IRequestHandler<LoginUserCommand, ApiResponse<LoginUserResult>> {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final IValidator<LoginUserCommand> validator;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

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
            return ApiResponse.badRequest("Invalid email or password");
        }

        // --- 3. Check if account is active ---
        if (user.getIsActive() == null || !user.getIsActive()) {
            return ApiResponse.badRequest("Account has been deactivated");
        }

        // --- 4. Verify password with BCrypt ---
        if (!passwordEncoder.matches(command.password(), user.getPassword())) {
            return ApiResponse.badRequest("Invalid email or password");
        }

        // --- 5. Check email verification ---
        boolean isVerified =
                emailVerificationRepository.existsByEmailAndVerifiedAtIsNotNull(user.getEmail());
        if (!isVerified) {
            throw new EmailNotVerifiedException(
                    "Please verify your email before logging in. Check your inbox.");
        }

        // --- 6. Generate JWT token ---
        String token = jwtUtil.generateToken(user.getEmail());

        // --- 7. Create result ---
        var result =
                new LoginUserResult(
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        token);

        // --- 8. Response ---
        return ApiResponse.success(result, "Login successful");
    }
}
