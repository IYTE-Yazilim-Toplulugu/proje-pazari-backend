package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import com.iyte_yazilim.proje_pazari.domain.models.IyteEmail;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RegisterUserHandler
        implements IRequestHandler<RegisterUserCommand, ApiResponse<RegisterUserResult>> {

    private final UserRepository userRepository;
    private final EmailVerificationRepository emailVerificationRepository;
    private final RegisterUserMapper registerUserMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final VerificationTokenService verificationTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final BusinessMetricsService metricsService;

    /**
     * Handles user registration command.
     *
     * <p>Performs validation, duplicate check, password encryption, and persists the new user to
     * the database.
     *
     * @param command the registration command containing user details
     * @return API response with registration result or error message
     */
    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<RegisterUserResult> handle(RegisterUserCommand command) {

        // --- 1. IYTE Email Value Object ---
        IyteEmail iyteEmail = IyteEmail.of(command.email());

        // --- 2. Check if email already exists ---
        if (userRepository.existsByEmail(iyteEmail.getValue())) {
            metricsService.incrementUserRegistrationFailure();
            return ApiResponse.badRequest(
                    messageService.getMessage("auth.email.already.registered"));
        }

        // --- 3. Mapping (Command -> Domain Entity) ---
        User domainUser = registerUserMapper.commandToDomain(command);

        // --- 4. Hash password ---
        String hashedPassword = passwordEncoder.encode(command.password());
        domainUser.setPassword(hashedPassword);

        // --- 5. Mapping (Domain -> Persistence) ---
        UserEntity persistenceUser = userMapper.domainToEntity(domainUser);

        // --- 6. Persistence ---
        UserEntity savedUser = userRepository.save(persistenceUser);

        // --- 7. Create email verification record ---
        String verificationToken = verificationTokenService.generateToken();
        EmailVerificationEntity emailVerification = new EmailVerificationEntity();
        emailVerification.setUserId(savedUser.getId());
        emailVerification.setEmail(savedUser.getEmail());
        emailVerification.setToken(verificationToken);
        emailVerification.setExpiresAt(verificationTokenService.calculateExpirationDate());
        emailVerificationRepository.save(emailVerification);

        // --- 8. Mapping (Persistence -> Domain) ---
        User savedDomainUser = userMapper.entityToDomain(savedUser);

        // --- 9. Publish verification event ---
        eventPublisher.publishEvent(
                new UserRegisteredEvent(
                        savedDomainUser.getId(),
                        savedDomainUser.getEmail(),
                        savedDomainUser.getFirstName(),
                        verificationToken));

        // --- 10. Result Mapping (Domain Entity -> Result DTO) ---
        var result = registerUserMapper.domainToResult(savedDomainUser);

        // --- 11. Response with localized message ---
        metricsService.incrementUserRegistrationSuccess();
        return ApiResponse.registeredNeedsVerification(
                result, messageService.getMessage("user.registered.success"));
    }
}
