package com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.events.VerificationEmailRequestedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailAlreadyVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendVerificationEmailHandler
        implements IRequestHandler<ResendVerificationEmailCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final VerificationTokenService verificationTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ApiResponse<Void> handle(ResendVerificationEmailCommand command) {
        UserEntity userEntity =
                userRepository
                        .findByEmail(command.email())
                        .orElseThrow(() -> new UserNotFoundException(command.email()));

        // Check if already verified
        boolean isVerified =
                emailVerificationRepository.existsByEmailAndVerifiedAtIsNotNull(command.email());
        if (isVerified) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        // Find existing unverified verification or create new one
        EmailVerificationEntity verification =
                emailVerificationRepository
                        .findByEmailAndVerifiedAtIsNull(command.email())
                        .orElse(new EmailVerificationEntity());

        // Generate new token
        String token = verificationTokenService.generateToken();
        verification.setUserId(userEntity.getId());
        verification.setEmail(userEntity.getEmail());
        verification.setToken(token);
        verification.setExpiresAt(verificationTokenService.calculateExpirationDate());
        verification.setVerifiedAt(null);

        emailVerificationRepository.save(verification);

        // Publish event
        eventPublisher.publishEvent(
                new VerificationEmailRequestedEvent(
                        Ulid.from(userEntity.getId()),
                        userEntity.getEmail(),
                        userEntity.getFirstName(),
                        token));

        return ApiResponse.success(null, "Verification email sent");
    }
}
