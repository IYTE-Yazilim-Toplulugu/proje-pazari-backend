package com.iyte_yazilim.proje_pazari.application.commands.loginUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IValidator;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.LoginUserResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginUserHandlerTest {

    @Mock private UserRepository userRepository;

    @Mock private IValidator<LoginUserCommand> validator;

    @Mock private PasswordEncoder passwordEncoder;

    @Mock private JwtUtil jwtUtil;

    @InjectMocks private LoginUserHandler handler;

    @Test
    @DisplayName("Should login successfully when credentials are valid")
    void shouldLogin_whenCredentialsAreValid() {
        // Given
        String userId = Ulid.fast().toString();
        String email = "test@std.iyte.edu.tr";
        String password = "SecurePass123!";
        String encodedPassword = "encoded-password";
        String jwtToken = "jwt.token.here";

        LoginUserCommand command = new LoginUserCommand(email, password);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userEntity.setPassword(encodedPassword);
        userEntity.setFirstName("John");
        userEntity.setLastName("Doe");
        userEntity.setIsActive(true);

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtUtil.generateToken(email)).thenReturn(jwtToken);

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Login successful", response.getMessage());
        assertNotNull(response.getData());
        assertEquals(userId, response.getData().userId());
        assertEquals(email, response.getData().email());
        assertEquals("John", response.getData().firstName());
        assertEquals("Doe", response.getData().lastName());
        assertEquals(jwtToken, response.getData().token());
    }

    @Test
    @DisplayName("Should return error when validation fails")
    void shouldReturnError_whenValidationFails() {
        // Given
        LoginUserCommand command = new LoginUserCommand("", "");

        when(validator.validate(command))
                .thenReturn(new String[] {"Email is required", "Password is required"});

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertTrue(response.getMessage().contains("Email is required"));
        assertTrue(response.getMessage().contains("Password is required"));
        verify(userRepository, never()).findByEmail(anyString());
    }

    @Test
    @DisplayName("Should return error when user not found")
    void shouldReturnError_whenUserNotFound() {
        // Given
        String email = "nonexistent@std.iyte.edu.tr";
        LoginUserCommand command = new LoginUserCommand(email, "password123");

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Invalid email or password", response.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return error when account is deactivated")
    void shouldReturnError_whenAccountIsDeactivated() {
        // Given
        String email = "deactivated@std.iyte.edu.tr";
        LoginUserCommand command = new LoginUserCommand(email, "password123");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(Ulid.fast().toString());
        userEntity.setEmail(email);
        userEntity.setIsActive(false);

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Account has been deactivated", response.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return error when isActive is null")
    void shouldReturnError_whenIsActiveIsNull() {
        // Given
        String email = "nullactive@std.iyte.edu.tr";
        LoginUserCommand command = new LoginUserCommand(email, "password123");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(Ulid.fast().toString());
        userEntity.setEmail(email);
        userEntity.setIsActive(null);

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Account has been deactivated", response.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Should return error when password is incorrect")
    void shouldReturnError_whenPasswordIsIncorrect() {
        // Given
        String email = "test@std.iyte.edu.tr";
        String wrongPassword = "WrongPassword123!";
        String encodedPassword = "encoded-correct-password";

        LoginUserCommand command = new LoginUserCommand(email, wrongPassword);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(Ulid.fast().toString());
        userEntity.setEmail(email);
        userEntity.setPassword(encodedPassword);
        userEntity.setIsActive(true);

        when(validator.validate(command)).thenReturn(null);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(wrongPassword, encodedPassword)).thenReturn(false);

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        assertEquals("Invalid email or password", response.getMessage());
        verify(jwtUtil, never()).generateToken(anyString());
    }

    @Test
    @DisplayName("Should proceed when validator returns empty array")
    void shouldProceed_whenValidatorReturnsEmptyArray() {
        // Given
        String userId = Ulid.fast().toString();
        String email = "test@std.iyte.edu.tr";
        String password = "SecurePass123!";
        String jwtToken = "jwt.token.here";

        LoginUserCommand command = new LoginUserCommand(email, password);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail(email);
        userEntity.setPassword("encoded-password");
        userEntity.setFirstName("Jane");
        userEntity.setLastName("Smith");
        userEntity.setIsActive(true);

        when(validator.validate(command)).thenReturn(new String[] {});
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(password, "encoded-password")).thenReturn(true);
        when(jwtUtil.generateToken(email)).thenReturn(jwtToken);

        // When
        ApiResponse<LoginUserResult> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNotNull(response.getData());
        assertEquals(jwtToken, response.getData().token());
    }
}
