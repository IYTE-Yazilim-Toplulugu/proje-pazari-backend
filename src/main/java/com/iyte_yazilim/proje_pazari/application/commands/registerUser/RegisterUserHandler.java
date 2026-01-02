package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.UserRegisteredEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the {@link RegisterUserCommand} to register new users.
 *
 * <p>This handler orchestrates the user registration process:
 *
 * <ol>
 *   <li>Validate command using {@link RegisterUserValidator}
 *   <li>Check for duplicate email addresses
 *   <li>Map command to domain entity
 *   <li>Encrypt password using BCrypt
 *   <li>Persist user to database
 *   <li>Return registration result
 * </ol>
 *
 * <h2>Error Scenarios:</h2>
 *
 * <ul>
 *   <li>{@code BAD_REQUEST} - Validation failed
 *   <li>{@code BAD_REQUEST} - Email already registered
 * </ul>
 *
 * <h2>Example:</h2>
 *
 * <pre>{@code
 * RegisterUserCommand command = new RegisterUserCommand(...);
 * ApiResponse<RegisterUserResult> response = handler.handle(command);
 *
 * if (response.getCode() == ResponseCode.CREATED) {
 *     String userId = response.getData().userId();
 *     // User registered successfully
 * }
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see RegisterUserCommand
 * @see RegisterUserResult
 * @see UserRepository
 */
@Service
@RequiredArgsConstructor
public class RegisterUserHandler
        implements IRequestHandler<RegisterUserCommand, ApiResponse<RegisterUserResult>> {

    private final UserRepository userRepository;
    private final IValidator<RegisterUserCommand> validator;
    private final RegisterUserMapper registerUserMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final MessageService messageService;
    private final ApplicationEventPublisher applicationEventPublisher;

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

        // --- 1. Validation ---
        var errors = validator.validate(command);
        if (errors != null && errors.length > 0) {
            String errorMessage = String.join(", ", errors);
            return ApiResponse.badRequest(errorMessage);
        }

        // --- 2. Check if email already exists ---
        if (userRepository.existsByEmail(command.email())) {
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

        // --- 7. Mapping (Persistence -> Domain) ---
        User savedDomainUser = userMapper.entityToDomain(savedUser);

        // --- 8. Result Mapping (Domain Entity -> Result DTO) ---
        var result = registerUserMapper.domainToResult(savedDomainUser);

        String verificationToken = Ulid.fast().toString();

        // Publish event for side effects (email sending handled by EmailEventListener)
        applicationEventPublisher.publishEvent(
                new UserRegisteredEvent(
                        savedDomainUser.getId().toString(),
                        savedDomainUser.getEmail(),
                        savedDomainUser.getFirstName(),
                        verificationToken,
                        LocalDateTime.now()));

        // --- 9. Response with localized message ---
        return ApiResponse.created(result, messageService.getMessage("user.registered.success"));
    }
}
