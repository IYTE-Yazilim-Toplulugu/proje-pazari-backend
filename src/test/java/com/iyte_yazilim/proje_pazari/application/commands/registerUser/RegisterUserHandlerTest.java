package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private RegisterUserMapper registerUserMapper;

    @Mock private UserMapper userMapper;

    @Mock private IValidator<RegisterUserCommand> validator;

    @InjectMocks private RegisterUserHandler handler;

    @Test
    @DisplayName("Should register user successfully when valid command provided")
    void shouldRegisterUser_whenValidCommand() {
        // Given
        RegisterUserCommand command =
                new RegisterUserCommand("test@std.iyte.edu.tr", "password123", "John", "Doe");

        User domainUser = new User();
        domainUser.setEmail(command.email());
        domainUser.setFirstName(command.firstName());
        domainUser.setLastName(command.lastName());

        UserEntity userEntity = new UserEntity();
        userEntity.setId("01HQXYZ123");
        userEntity.setEmail(command.email());

        User savedDomainUser = new User();
        savedDomainUser.setEmail(command.email());
        savedDomainUser.setFirstName(command.firstName());
        savedDomainUser.setLastName(command.lastName());

        RegisterUserResult expectedResult =
                new RegisterUserResult(
                        "01HQXYZ123", command.email(), command.firstName(), command.lastName());

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.existsByEmail(command.email())).thenReturn(false);
        when(registerUserMapper.commandToDomain(command)).thenReturn(domainUser);
        when(passwordEncoder.encode(command.password())).thenReturn("encoded-password");
        when(userMapper.domainToEntity(domainUser)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.entityToDomain(userEntity)).thenReturn(savedDomainUser);
        when(registerUserMapper.domainToResult(savedDomainUser)).thenReturn(expectedResult);

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.CREATED, response.getCode());
        assertNotNull(response.getData());
        assertEquals(expectedResult.userId(), response.getData().userId());
        assertEquals(expectedResult.email(), response.getData().email());
        verify(userRepository).save(userEntity);
        verify(passwordEncoder).encode(command.password());
    }

    @Test
    @DisplayName("Should return error when email already exists")
    void shouldReturnError_whenEmailExists() {
        // Given
        RegisterUserCommand command =
                new RegisterUserCommand("existing@std.iyte.edu.tr", "password123", "John", "Doe");

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.existsByEmail(command.email())).thenReturn(true);

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertTrue(response.getMessage().contains("Email already registered"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for non-IYTE email")
    void shouldReturnValidationError_whenNonIyteEmail() {
        // Given
        RegisterUserCommand command =
                new RegisterUserCommand("test@gmail.com", "password123", "John", "Doe");

        when(validator.validate(command))
                .thenReturn(new String[] {"Email must be an IYTE email address"});

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertTrue(response.getMessage().contains("IYTE"));
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }
}
