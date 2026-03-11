package com.iyte_yazilim.proje_pazari.application.commands.promoteToProjectOwner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromoteToProjectOwnerHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private MessageService messageService;

    private PromoteToProjectOwnerHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PromoteToProjectOwnerHandler(userRepository, messageService);
    }

    @Nested
    @DisplayName("handle() method")
    class HandleTests {

        @Test
        @DisplayName("should promote USER to PROJECT_OWNER successfully")
        void shouldPromoteUserToProjectOwner() {
            // Given
            String userId = "01HQXYZ123";
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setEmail("user@std.iyte.edu.tr");
            user.setRole(RoleType.APPLICANT);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(messageService.getMessage("user.promoted.to.project.owner"))
                    .thenReturn("User promoted to PROJECT_OWNER successfully");

            PromoteToProjectOwnerCommand command = new PromoteToProjectOwnerCommand(userId);

            // When
            ApiResponse<Void> response = handler.handle(command);

            // Then
            assertEquals(ResponseCode.SUCCESS, response.getCode());
            assertEquals("User promoted to PROJECT_OWNER successfully", response.getMessage());
            assertEquals(RoleType.PROJECT_OWNER, user.getRole());
            verify(userRepository).save(user);
        }

        @Test
        @DisplayName("should promote APPLICANT to PROJECT_OWNER successfully")
        void shouldPromoteApplicantToProjectOwner() {
            // Given
            String userId = "01HQXYZ123";
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setEmail("applicant@std.iyte.edu.tr");
            user.setRole(RoleType.APPLICANT);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(userRepository.save(any(UserEntity.class))).thenReturn(user);
            when(messageService.getMessage("user.promoted.to.project.owner"))
                    .thenReturn("User promoted to PROJECT_OWNER successfully");

            PromoteToProjectOwnerCommand command = new PromoteToProjectOwnerCommand(userId);

            // When
            ApiResponse<Void> response = handler.handle(command);

            // Then
            assertEquals(ResponseCode.SUCCESS, response.getCode());
            assertEquals(RoleType.PROJECT_OWNER, user.getRole());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when user does not exist")
        void shouldThrowException_whenUserDoesNotExist() {
            // Given
            String userId = "nonexistent";
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            PromoteToProjectOwnerCommand command = new PromoteToProjectOwnerCommand(userId);

            // When & Then
            assertThrows(UserNotFoundException.class, () -> handler.handle(command));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return VALIDATION_ERROR when user is already PROJECT_OWNER")
        void shouldReturnValidationErrorWhenAlreadyProjectOwner() {
            // Given
            String userId = "01HQXYZ123";
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setEmail("owner@std.iyte.edu.tr");
            user.setRole(RoleType.PROJECT_OWNER);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(messageService.getMessage("user.already.project.owner"))
                    .thenReturn("User is already a PROJECT_OWNER");

            PromoteToProjectOwnerCommand command = new PromoteToProjectOwnerCommand(userId);

            // When
            ApiResponse<Void> response = handler.handle(command);

            // Then
            assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
            assertEquals("User is already a PROJECT_OWNER", response.getMessage());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return VALIDATION_ERROR when trying to demote ADMIN")
        void shouldReturnValidationErrorWhenTryingToDemoteAdmin() {
            // Given
            String userId = "01HQXYZ123";
            UserEntity user = new UserEntity();
            user.setId(userId);
            user.setEmail("admin@std.iyte.edu.tr");
            user.setRole(RoleType.ADMIN);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(messageService.getMessage("admin.cannot.be.demoted"))
                    .thenReturn("ADMIN users cannot be demoted");

            PromoteToProjectOwnerCommand command = new PromoteToProjectOwnerCommand(userId);

            // When
            ApiResponse<Void> response = handler.handle(command);

            // Then
            assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
            assertEquals("ADMIN users cannot be demoted", response.getMessage());
            verify(userRepository, never()).save(any());
        }
    }
}
