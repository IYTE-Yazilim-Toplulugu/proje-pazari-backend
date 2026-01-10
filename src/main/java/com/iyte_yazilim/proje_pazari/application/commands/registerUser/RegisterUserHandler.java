package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public ApiResponse<RegisterUserResult> handle(RegisterUserCommand command) {

        // --- 1. Validation ---
        var errors = validator.validate(command);
        if (errors != null && errors.length > 0) {
            String errorMessage = String.join(", ", errors);
            return ApiResponse.badRequest(errorMessage);
        }

        // --- 2. Check if email already exists ---
        if (userRepository.existsByEmail(command.email())) {
            return ApiResponse.badRequest("Email already registered");
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

        // --- 9. Response ---
        return ApiResponse.created(result, "User registered successfully");
    }
}
