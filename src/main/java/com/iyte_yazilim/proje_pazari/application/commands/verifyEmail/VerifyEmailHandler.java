package com.iyte_yazilim.proje_pazari.application.commands.verifyEmail;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailAlreadyVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailHandler
        implements IRequestHandler<VerifyEmailCommand, ApiResponse<VerifyEmailResult>> {

    private final EmailVerificationRepository emailVerificationRepository;

    @Override
    @Transactional
    public ApiResponse<VerifyEmailResult> handle(VerifyEmailCommand command) {

        // Find verification record by token
        var verification =
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

        // Create result with Ulid
        VerifyEmailResult result =
                new VerifyEmailResult(
                        Ulid.from(verification.getUserId()), // String -> Ulid conversion
                        verification.getEmail(),
                        "Email verified successfully");

        return ApiResponse.success(result, "Email verified successfully");
    }
}
