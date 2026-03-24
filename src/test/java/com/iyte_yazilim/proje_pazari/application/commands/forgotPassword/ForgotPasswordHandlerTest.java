package com.iyte_yazilim.proje_pazari.application.commands.forgotPassword;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.services.VerificationTokenService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.PasswordResetTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ForgotPasswordHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private VerificationTokenService verificationTokenService;
    @Mock private EmailService emailService;
    @Mock private MessageService messageService;

    @InjectMocks private ForgotPasswordHandler handler;

    private static final String SUCCESS_MSG = "Reset email sent";

    @BeforeEach
    void setUp() {
        lenient()
                .when(messageService.getMessage("auth.password.reset.email.sent"))
                .thenReturn(SUCCESS_MSG);
    }

    @Test
    @DisplayName("Should return success and send email when user exists")
    void shouldSendEmailAndReturnSuccess_WhenUserExists() {
        UserEntity user = new UserEntity();
        user.setId("user-123");
        user.setEmail("student@std.iyte.edu.tr");
        user.setFirstName("Ali");

        when(userRepository.findByEmail("student@std.iyte.edu.tr")).thenReturn(Optional.of(user));
        when(verificationTokenService.generateToken()).thenReturn("test-token-uuid");

        ApiResponse<Void> response =
                handler.handle(new ForgotPasswordCommand("student@std.iyte.edu.tr"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(SUCCESS_MSG, response.getMessage());
        verify(passwordResetTokenRepository).deleteByUserId("user-123");
        verify(passwordResetTokenRepository).save(any());
        verify(emailService).sendTemplateEmailAsync(eq("student@std.iyte.edu.tr"), eq("password-reset.html"), any());
    }

    @Test
    @DisplayName("Should return success without sending email when user does not exist")
    void shouldReturnSuccessWithoutEmail_WhenUserNotFound() {
        when(userRepository.findByEmail("nobody@std.iyte.edu.tr")).thenReturn(Optional.empty());

        ApiResponse<Void> response =
                handler.handle(new ForgotPasswordCommand("nobody@std.iyte.edu.tr"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verifyNoInteractions(passwordResetTokenRepository, emailService, verificationTokenService);
    }
}
