package com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.VerificationEmailRequestedEvent;
import com.iyte_yazilim.proje_pazari.domain.exceptions.EmailAlreadyVerifiedException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResendVerificationEmailHandler
        implements IRequestHandler<ResendVerificationEmailCommand, ApiResponse<Void>> {

    private final UserRepository userRepository;
    private final VerificationTokenService verificationTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final UserMapper userMapper;

    @Override
    public ApiResponse<Void> handle(ResendVerificationEmailCommand command) {
        UserEntity userEntity =
                userRepository
                        .findByEmail(command.email())
                        .orElseThrow(() -> new UserNotFoundException(command.email()));

        if (userEntity.isEmailVerified()) {
            throw new EmailAlreadyVerifiedException("Email is already verified");
        }

        // Map to domain
        User user = userMapper.entityToDomain(userEntity);

        // Generate new token
        String token = verificationTokenService.generateTokenWithExpiry(user);

        // Update entity
        userEntity.setVerificationToken(user.getVerificationToken());
        userEntity.setVerificationTokenExpiresAt(user.getVerificationTokenExpiresAt());
        userRepository.save(userEntity);

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
