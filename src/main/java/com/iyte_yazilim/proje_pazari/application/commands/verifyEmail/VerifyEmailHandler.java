package com.iyte_yazilim.proje_pazari.application.commands.verifyEmail;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailAlreadyVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class VerifyEmailHandler implements IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>> {

    private final UserRepository userRepository;
    private final VerificationTokenService tokenService;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<VerifyEmailResult> handle(VerifyEmailCommand command) {
        // Find user by verification token
        UserEntity userEntity = userRepository.findByVerificationToken(command.token())
                .orElseThrow(() -> new InvalidVerificationTokenException("Invalid verification token"));

        // Check if already verified
        if (userEntity.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        // Check if token is expired
        if (tokenService.isTokenExpired(userEntity.getVerificationTokenExpiresAt())) {
            throw new VerificationTokenExpiredException("Verification token has expired");
        }

        // Verify email
        userEntity.setEmailVerified(true);
        userEntity.setEmailVerifiedAt(LocalDateTime.now());
        userEntity.setVerificationToken(null);
        userEntity.setVerificationTokenExpiresAt(null);

        UserEntity savedEntity = userRepository.save(userEntity);
        User savedUser = userMapper.entityToDomain(savedEntity);

        VerifyEmailResult result = new VerifyEmailResult(
                savedUser.getId(),
                savedUser.getEmail(),
                "Email verified successfully"
        );

        return ApiResponse.success(result, "Email verified successfully");
    }
}
