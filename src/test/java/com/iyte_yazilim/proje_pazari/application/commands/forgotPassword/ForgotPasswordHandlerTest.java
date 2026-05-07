package com.iyte_yazilim.proje_pazari.application.commands.forgotPassword;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.events.PasswordResetEmailRequestedEvent;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IPasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IUserRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordHandlerTest {

    @Mock private IUserRepository userRepository;
    @Mock private IPasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private VerificationTokenService verificationTokenService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MessageService messageService;
    @Mock private Clock clock;

    @InjectMocks private ForgotPasswordHandler handler;

    private static final String SUCCESS_MSG = "Reset email sent";
    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String RESET_PATH = "/reset-password";

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("UTC"));
        lenient().when(clock.instant()).thenReturn(fixedClock.instant());
        lenient().when(clock.getZone()).thenReturn(fixedClock.getZone());

        ReflectionTestUtils.setField(handler, "frontendUrl", FRONTEND_URL);
        ReflectionTestUtils.setField(handler, "resetPasswordPath", RESET_PATH);
        lenient()
                .when(messageService.getMessage("auth.password.reset.email.sent"))
                .thenReturn(SUCCESS_MSG);
    }

    @Test
    @DisplayName("Should return success and publish reset email event when user exists")
    void shouldPublishEventAndReturnSuccess_WhenUserExists() {
        User user = new User("student@std.iyte.edu.tr", "hashed", "Ali", "Test");
        ReflectionTestUtils.setField(
                user, "id", com.github.f4b6a3.ulid.Ulid.from("01HRQJ4FPGESQHPKS0MRCYG9M0"));

        when(userRepository.findByEmail("student@std.iyte.edu.tr")).thenReturn(Optional.of(user));
        when(verificationTokenService.generateToken()).thenReturn("test-token-uuid");
        when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApiResponse<Void> response =
                handler.handle(new ForgotPasswordCommand("student@std.iyte.edu.tr"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(SUCCESS_MSG, response.getMessage());
        verify(passwordResetTokenRepository).deleteByUserId("01HRQJ4FPGESQHPKS0MRCYG9M0");
        verify(passwordResetTokenRepository).save(any());

        ArgumentCaptor<PasswordResetEmailRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(PasswordResetEmailRequestedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        PasswordResetEmailRequestedEvent published = eventCaptor.getValue();
        assertEquals("student@std.iyte.edu.tr", published.getEmail());
        assertEquals(
                "http://localhost:3000/reset-password?token=test-token-uuid",
                published.getResetLink());
    }

    @Test
    @DisplayName("Should return success without publishing event when user does not exist")
    void shouldReturnSuccessWithoutEvent_WhenUserNotFound() {
        when(userRepository.findByEmail("nobody@std.iyte.edu.tr")).thenReturn(Optional.empty());

        ApiResponse<Void> response =
                handler.handle(new ForgotPasswordCommand("nobody@std.iyte.edu.tr"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verifyNoInteractions(
                passwordResetTokenRepository, eventPublisher, verificationTokenService);
    }
}
