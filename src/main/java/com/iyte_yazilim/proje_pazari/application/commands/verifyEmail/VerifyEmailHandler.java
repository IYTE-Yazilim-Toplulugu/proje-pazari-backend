package com.iyte_yazilim.proje_pazari.application.commands.verifyEmail;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailAlreadyVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyEmailHandler
        implements IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>> {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final VerificationTokenService tokenService;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<VerifyEmailResult> handle(VerifyEmailCommand command) {
        // Find verification record by token
        EmailVerificationEntity verification =
                emailVerificationRepository
                        .findByToken(command.token())
                        .orElseThrow(
                                () ->
                                        new InvalidVerificationTokenException(
                                                "Invalid verification token"));

        // Check if already verified
        if (verification.isVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        // Check if token is expired
        if (verification.isExpired()) {
            throw new VerificationTokenExpiredException("Verification token has expired");
        }

        // Mark as verified
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        // Get user
        UserEntity userEntity =
                userRepository
                        .findById(verification.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));

        User savedUser = userMapper.entityToDomain(userEntity);

        VerifyEmailResult result =
                new VerifyEmailResult(
                        savedUser.getId(), savedUser.getEmail(), "Email verified successfully");

        return ApiResponse.success(result, "Email verified successfully");
    }
}
