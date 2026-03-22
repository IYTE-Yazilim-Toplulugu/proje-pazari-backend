package com.iyte_yazilim.proje_pazari.application.commands.registerUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.mappers.RegisterUserMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.metrics.BusinessMetricsService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegisterUserHandlerTest {

    @Mock private UserRepository userRepository;

    @Mock private EmailVerificationRepository emailVerificationRepository;

    @Mock private RegisterUserMapper registerUserMapper;

    @Mock private UserMapper userMapper;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private VerificationTokenService verificationTokenService;

    @Mock private ApplicationEventPublisher eventPublisher;

    @Mock private MessageService messageService;

    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @Mock private BusinessMetricsService metricsService;

    @InjectMocks private RegisterUserHandler handler;

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("user.registered.success"))
                .thenReturn("User registered successfully");
        lenient()
                .when(messageService.getMessage("auth.email.already.registered"))
                .thenReturn("Email already registered");
    }

    @Test
    @DisplayName("Should register user successfully when valid command provided")
    void shouldRegisterUser_whenValidCommand() {
        // Given
        String email = "test@std.iyte.edu.tr";
        Ulid userId = Ulid.fast();

        RegisterUserCommand command = new RegisterUserCommand(email, "password123", "John", "Doe");

        User domainUser = new User();
        domainUser.setEmail(email);
        domainUser.setFirstName(command.firstName());
        domainUser.setLastName(command.lastName());

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId.toString());
        userEntity.setEmail(email);

        User savedDomainUser = new User();
        savedDomainUser.setId(userId);
        savedDomainUser.setEmail(email);
        savedDomainUser.setFirstName(command.firstName());
        savedDomainUser.setLastName(command.lastName());

        RegisterUserResult expectedResult =
                new RegisterUserResult(
                        userId.toString(), email, command.firstName(), command.lastName());

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(registerUserMapper.commandToDomain(command)).thenReturn(domainUser);
        when(passwordEncoder.encode(command.password())).thenReturn("encoded-password");
        when(userMapper.domainToEntity(domainUser)).thenReturn(userEntity);
        when(userRepository.save(userEntity)).thenReturn(userEntity);
        when(userMapper.entityToDomain(userEntity)).thenReturn(savedDomainUser);
        when(registerUserMapper.domainToResult(savedDomainUser)).thenReturn(expectedResult);
        when(verificationTokenService.generateToken()).thenReturn("verification-token");
        when(verificationTokenService.calculateExpirationDate())
                .thenReturn(LocalDateTime.now().plusHours(24));

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.CREATED, response.getCode());
        assertNotNull(response.getData());
        assertEquals(expectedResult.userId(), response.getData().userId());
        assertEquals(expectedResult.email(), response.getData().email());
        verify(userRepository).save(userEntity);
        verify(passwordEncoder).encode(command.password());
        verify(metricsService).incrementUserRegistrationSuccess();
    }

    @Test
    @DisplayName("Should return error when email already exists")
    void shouldReturnError_whenEmailExists() {
        // Given
        String email = "existing@std.iyte.edu.tr";
        RegisterUserCommand command = new RegisterUserCommand(email, "password123", "John", "Doe");

        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        ApiResponse<RegisterUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Email already registered", response.getMessage());
        verify(userRepository, never()).save(any());
        verify(metricsService).incrementUserRegistrationFailure();
    }
}
