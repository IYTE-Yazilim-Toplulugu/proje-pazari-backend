package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.security.service.RefreshTokenService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ResetPasswordHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private MessageService messageService;

    @InjectMocks private ResetPasswordHandler handler;

    private static final String VALID_PASSWORD = "NewSecurePass1!";
    private static final String TOKEN = "valid-reset-token";

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("auth.password.reset.token.invalid"))
                .thenReturn("Invalid token");
        lenient()
                .when(messageService.getMessage("auth.password.reset.token.expired"))
                .thenReturn("Token expired");
        lenient()
                .when(messageService.getMessage("validation.password.mismatch"))
                .thenReturn("Passwords do not match");
        lenient()
                .when(messageService.getMessage("validation.password.requirements"))
                .thenReturn("Password too weak");
        lenient()
                .when(messageService.getMessage("auth.password.reset.success"))
                .thenReturn("Password reset successfully");
    }

    private PasswordResetTokenEntity validToken() {
        PasswordResetTokenEntity t = new PasswordResetTokenEntity();
        t.setToken(TOKEN);
        t.setUserId("user-123");
        t.setEmail("student@std.iyte.edu.tr");
        t.setExpiresAt(LocalDateTime.now().plusHours(1));
        return t;
    }

    private UserEntity userWithId(String id) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setPassword("old-hashed-password");
        return u;
    }

    @Test
    @DisplayName("Should reset password successfully when token and password are valid")
    void shouldResetPassword_WhenTokenAndPasswordAreValid() {
        PasswordResetTokenEntity token = validToken();
        UserEntity user = userWithId("user-123");

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("new-hashed-password");

        ApiResponse<Void> response =
                handler.handle(new ResetPasswordCommand(TOKEN, VALID_PASSWORD, VALID_PASSWORD));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("new-hashed-password", user.getPassword());
        assertNotNull(token.getUsedAt());
        verify(refreshTokenService).revokeAllUserTokens("user-123");
    }

    @Test
    @DisplayName("Should throw InvalidVerificationTokenException when token not found")
    void shouldThrow_WhenTokenNotFound() {
        when(passwordResetTokenRepository.findByToken("bad-token")).thenReturn(Optional.empty());

        assertThrows(
                InvalidVerificationTokenException.class,
                () ->
                        handler.handle(
                                new ResetPasswordCommand(
                                        "bad-token", VALID_PASSWORD, VALID_PASSWORD)));
    }

    @Test
    @DisplayName("Should throw InvalidVerificationTokenException when token already used")
    void shouldThrow_WhenTokenAlreadyUsed() {
        PasswordResetTokenEntity token = validToken();
        token.setUsedAt(LocalDateTime.now().minusMinutes(5));

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));

        assertThrows(
                InvalidVerificationTokenException.class,
                () ->
                        handler.handle(
                                new ResetPasswordCommand(TOKEN, VALID_PASSWORD, VALID_PASSWORD)));
    }

    @Test
    @DisplayName("Should throw VerificationTokenExpiredException when token is expired")
    void shouldThrow_WhenTokenExpired() {
        PasswordResetTokenEntity token = validToken();
        token.setExpiresAt(LocalDateTime.now().minusHours(2));

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));

        assertThrows(
                VerificationTokenExpiredException.class,
                () ->
                        handler.handle(
                                new ResetPasswordCommand(TOKEN, VALID_PASSWORD, VALID_PASSWORD)));
    }

    @Test
    @DisplayName("Should return validation error when passwords do not match")
    void shouldReturnValidationError_WhenPasswordsDoNotMatch() {
        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(validToken()));

        ApiResponse<Void> response =
                handler.handle(new ResetPasswordCommand(TOKEN, VALID_PASSWORD, "DifferentPass1!"));

        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        verifyNoInteractions(userRepository, passwordEncoder, refreshTokenService);
    }

    @Test
    @DisplayName("Should return validation error when password is too weak")
    void shouldReturnValidationError_WhenPasswordIsWeak() {
        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(validToken()));

        ApiResponse<Void> response =
                handler.handle(new ResetPasswordCommand(TOKEN, "weak", "weak"));

        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        verifyNoInteractions(userRepository, passwordEncoder, refreshTokenService);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user no longer exists")
    void shouldThrow_WhenUserNotFound() {
        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(validToken()));
        when(userRepository.findById("user-123")).thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () ->
                        handler.handle(
                                new ResetPasswordCommand(TOKEN, VALID_PASSWORD, VALID_PASSWORD)));
    }
}
