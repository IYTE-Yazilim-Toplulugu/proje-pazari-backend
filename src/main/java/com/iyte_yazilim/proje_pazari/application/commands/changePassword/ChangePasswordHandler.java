package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChangePasswordHandler
        implements IRequestHandler<ChangePasswordCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<Void> handle(ChangePasswordCommand command) {
        try {
            command.validate();
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        }

        UserEntity user = userRepository.findById(command.userId()).orElse(null);

        if (user == null) {
            return ApiResponse.notFound("User not found");
        }

        // Verify current password
        if (!passwordEncoder.matches(command.currentPassword(), user.getPassword())) {
            return ApiResponse.validationError("Current password is incorrect");
        }

        // Validate new password strength
        if (!isPasswordStrong(command.newPassword())) {
            return ApiResponse.validationError(
                    "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one digit, and one special character");
        }

        // Hash and save new password
        user.setPassword(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        return ApiResponse.success(null, "Password changed successfully");
    }

    private boolean isPasswordStrong(String password) {
        // At least 8 characters, one uppercase, one lowercase, one digit, one special char
        return password.length() >= 8
                && password.matches(".*[A-Z].*")
                && password.matches(".*[a-z].*")
                && password.matches(".*\\d.*")
                && password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }
}
