package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
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

        // --- 9. Response with localized message ---
        return ApiResponse.created(result, messageService.getMessage("user.registered.success"));
    }
}
