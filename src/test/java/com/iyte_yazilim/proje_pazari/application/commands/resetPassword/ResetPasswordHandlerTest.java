package com.iyte_yazilim.proje_pazari.application.commands.resetPassword;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.PasswordResetToken;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.exceptions.InvalidVerificationTokenException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.VerificationTokenExpiredException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IPasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRefreshTokenService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ResetPasswordHandlerTest {

    @Mock private IUserRepository userRepository;
    @Mock private IPasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private IRefreshTokenService refreshTokenService;
    @Mock private MessageService messageService;

    private ResetPasswordHandler handler;

    private static final String VALID_PASSWORD = "NewSecurePass1!";
    private static final String TOKEN = "valid-reset-token";
    private static final Instant FIXED_INSTANT = Instant.parse("2025-01-15T12:00:00Z");
    private static final ZoneId ZONE = ZoneId.of("UTC");
    private final Clock fixedClock = Clock.fixed(FIXED_INSTANT, ZONE);
    private static final LocalDateTime FIXED_NOW =
            LocalDateTime.ofInstant(FIXED_INSTANT, ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        handler =
                new ResetPasswordHandler(
                        userRepository,
                        passwordResetTokenRepository,
                        passwordEncoder,
                        refreshTokenService,
                        messageService,
                        fixedClock);
        lenient()
                .when(messageService.getMessage("auth.password.reset.token.invalid"))
                .thenReturn("Invalid token");
        lenient()
                .when(messageService.getMessage("auth.password.reset.token.expired"))
                .thenReturn("Token expired");
        lenient()
                .when(messageService.getMessage("auth.password.reset.success"))
                .thenReturn("Password reset successfully");
    }

    private PasswordResetToken validToken() {
        PasswordResetToken t =
                new PasswordResetToken(
                        "user-123", "student@std.iyte.edu.tr", TOKEN, FIXED_NOW.plusHours(1));
        return t;
    }

    private User buildTestUser() {
        User u = new User("student@std.iyte.edu.tr", "old-hashed-password", "Ali", "Test");
        org.springframework.test.util.ReflectionTestUtils.setField(
                u, "id", com.github.f4b6a3.ulid.Ulid.from("01HRQJ4FPGESQHPKS0MRCYG9M0"));
        return u;
    }

    @Test
    @DisplayName("Should reset password successfully when token and password are valid")
    void shouldResetPassword_WhenTokenAndPasswordAreValid() {
        PasswordResetToken token = validToken();
        User user = buildTestUser();

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));
        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn("new-hashed-password");

        ApiResponse<Void> response =
                handler.handle(new ResetPasswordCommand(TOKEN, VALID_PASSWORD, VALID_PASSWORD));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("new-hashed-password", user.getPassword());
        assertEquals(FIXED_NOW, token.getUsedAt());
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
        PasswordResetToken token = validToken();
        token.setUsedAt(FIXED_NOW.minusMinutes(5));

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
        PasswordResetToken token = validToken();
        token.setExpiresAt(FIXED_NOW.minusHours(2));

        when(passwordResetTokenRepository.findByToken(TOKEN)).thenReturn(Optional.of(token));

        assertThrows(
                VerificationTokenExpiredException.class,
                () ->
                        handler.handle(
                                new ResetPasswordCommand(TOKEN, VALID_PASSWORD, VALID_PASSWORD)));
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
