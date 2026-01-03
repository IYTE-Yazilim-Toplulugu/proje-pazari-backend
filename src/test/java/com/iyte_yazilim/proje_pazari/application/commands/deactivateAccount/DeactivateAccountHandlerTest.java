package com.iyte_yazilim.proje_pazari.application.commands.deactivateAccount;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
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

@ExtendWith(MockitoExtension.class)
class DeactivateAccountHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private DeactivateAccountHandler handler;

    @Test
    @DisplayName("Should deactivate account successfully with reason")
    void shouldDeactivateAccount_whenUserExistsWithReason() {
        // Given
        String userId = Ulid.fast().toString();
        String reason = "No longer using the platform";
        DeactivateAccountCommand command = new DeactivateAccountCommand(userId, reason);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@std.iyte.edu.tr");
        userEntity.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When
        ApiResponse<Void> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Account deactivated successfully", response.getMessage());
        assertFalse(userEntity.getIsActive());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should deactivate account successfully without reason")
    void shouldDeactivateAccount_whenUserExistsWithoutReason() {
        // Given
        String userId = Ulid.fast().toString();
        DeactivateAccountCommand command = new DeactivateAccountCommand(userId, null);

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setEmail("test@std.iyte.edu.tr");
        userEntity.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When
        ApiResponse<Void> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("Account deactivated successfully", response.getMessage());
        assertFalse(userEntity.getIsActive());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should deactivate account when reason is blank")
    void shouldDeactivateAccount_whenReasonIsBlank() {
        // Given
        String userId = Ulid.fast().toString();
        DeactivateAccountCommand command = new DeactivateAccountCommand(userId, "   ");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setIsActive(true);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When
        ApiResponse<Void> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertFalse(userEntity.getIsActive());
        verify(userRepository).save(userEntity);
    }

    @Test
    @DisplayName("Should return not found error when user does not exist")
    void shouldReturnError_whenUserNotFound() {
        // Given
        String nonExistentUserId = Ulid.fast().toString();
        DeactivateAccountCommand command =
                new DeactivateAccountCommand(nonExistentUserId, "Leaving");

        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When
        ApiResponse<Void> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertEquals("User not found", response.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should deactivate already inactive account")
    void shouldDeactivateAccount_whenAlreadyInactive() {
        // Given
        String userId = Ulid.fast().toString();
        DeactivateAccountCommand command = new DeactivateAccountCommand(userId, "Re-deactivating");

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userId);
        userEntity.setIsActive(false);

        when(userRepository.findById(userId)).thenReturn(Optional.of(userEntity));

        // When
        ApiResponse<Void> response = handler.handle(command);

        // Then
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertFalse(userEntity.getIsActive());
        verify(userRepository).save(userEntity);
    }
}
