package com.iyte_yazilim.proje_pazari.application.commands.changePassword;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ChangePasswordHandlerTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ChangePasswordHandler changePasswordHandler;

    @Test
    @DisplayName("Should change password successfully when all conditions are met")
    void shouldChangePassword_WhenCredentialsAreValid() {
        // Given
        String userId = "user-123";
        String currentPass = "OldPass123!";
        String newPass = "NewStrongPass123!";

        ChangePasswordCommand command =
                new ChangePasswordCommand(userId, currentPass, newPass, newPass);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setPassword("encoded-old-password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(currentPass, "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode(newPass)).thenReturn("encoded-new-password");

        // When
        ApiResponse<Void> response = changePasswordHandler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Password changed successfully", response.getMessage());
        assertEquals("encoded-new-password", userEntity.getPassword());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should return error when user does not exist")
    void shouldReturnError_WhenUserNotFound() {
        // Given
        String newPass = "NewPass123!";
        ChangePasswordCommand command =
                new ChangePasswordCommand("unknown-user", "OldPass123!", newPass, newPass);

        when(userRepository.findById("unknown-user")).thenReturn(Optional.empty());

        // When
        ApiResponse<Void> response = changePasswordHandler.handle(command);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return error when current password is incorrect")
    void shouldReturnError_WhenCurrentPasswordIsIncorrect() {
        // Given
        String userId = "user-123";
        String wrongPass = "WrongPass123!";
        String newPass = "NewPass123!";

        ChangePasswordCommand command =
                new ChangePasswordCommand(userId, wrongPass, newPass, newPass);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setPassword("correct-encoded-password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(wrongPass, "correct-encoded-password")).thenReturn(false);

        // When
        ApiResponse<Void> response = changePasswordHandler.handle(command);

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertEquals("Current password is incorrect", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return error when new password is weak")
    void shouldReturnError_WhenNewPasswordIsWeak() {
        // Given
        String userId = "user-123";
        String currentPass = "OldPass123!";
        String weakPass = "weak";

        ChangePasswordCommand command =
                new ChangePasswordCommand(userId, currentPass, weakPass, weakPass);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setPassword("encoded-old-password");

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(currentPass, "encoded-old-password")).thenReturn(true);

        // When
        ApiResponse<Void> response = changePasswordHandler.handle(command);

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertTrue(response.getMessage().contains("Password must be at least 8 characters"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return error when password confirmation does not match")
    void shouldReturnError_WhenConfirmationDoesNotMatch() {
        // Given
        ChangePasswordCommand command =
                new ChangePasswordCommand(
                        "user-123", "OldPass123!", "NewPass123!", "DifferentPass123!");

        // When
        ApiResponse<Void> response = changePasswordHandler.handle(command);

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertTrue(response.getMessage().contains("do not match"));
        verify(userRepository, never()).findById(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return error when new password is same as current password")
    void shouldReturnError_WhenNewPasswordSameAsCurrent() {
        // Given
        String samePass = "SamePass123!";
        ChangePasswordCommand command =
                new ChangePasswordCommand("user-123", samePass, samePass, samePass);

        // When
        ApiResponse<Void> response = changePasswordHandler.handle(command);

        // Then
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        assertTrue(response.getMessage().contains("different from current"));
        verify(userRepository, never()).findById(anyString());
        verify(userRepository, never()).save(any());
    }
}
