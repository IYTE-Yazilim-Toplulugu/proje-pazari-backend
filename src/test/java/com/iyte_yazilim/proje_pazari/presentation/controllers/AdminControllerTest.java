package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.commands.promoteToProjectOwner.PromoteToProjectOwnerCommand;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private IRequestHandler<PromoteToProjectOwnerCommand, ApiResponse<Void>>
            promoteToProjectOwnerHandler;

    private AdminController adminController;

    @BeforeEach
    void setUp() {
        adminController = new AdminController(promoteToProjectOwnerHandler);
    }

    @Nested
    @DisplayName("promoteToProjectOwner() method")
    class PromoteToProjectOwnerTests {

        @Test
        @DisplayName("should return 200 OK when promotion succeeds")
        void shouldReturn200WhenPromotionSucceeds() {
            // Given
            String userId = "target-user-id";
            when(promoteToProjectOwnerHandler.handle(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    null, "User promoted to PROJECT_OWNER successfully"));

            // When
            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
            assertEquals(
                    "User promoted to PROJECT_OWNER successfully", response.getBody().getMessage());
        }

        @Test
        @DisplayName("should pass correct userId to handler")
        void shouldPassCorrectUserIdToHandler() {
            // Given
            String userId = "specific-user-id";
            when(promoteToProjectOwnerHandler.handle(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.success(null, "Success"));

            ArgumentCaptor<PromoteToProjectOwnerCommand> captor =
                    ArgumentCaptor.forClass(PromoteToProjectOwnerCommand.class);

            // When
            adminController.promoteToProjectOwner(userId);

            // Then
            verify(promoteToProjectOwnerHandler).handle(captor.capture());
            assertEquals(userId, captor.getValue().userId());
        }

        @Test
        @DisplayName("should return 404 NOT_FOUND when user does not exist")
        void shouldReturn404WhenUserNotFound() {
            // Given
            String userId = "nonexistent-user";
            when(promoteToProjectOwnerHandler.handle(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.notFound("User not found with id: nonexistent-user"));

            // When
            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(ResponseCode.NOT_FOUND, response.getBody().getCode());
            assertEquals(
                    "User not found with id: nonexistent-user", response.getBody().getMessage());
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when user is already PROJECT_OWNER")
        void shouldReturn400WhenAlreadyProjectOwner() {
            // Given
            String userId = "project-owner-id";
            when(promoteToProjectOwnerHandler.handle(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.validationError("User is already a PROJECT_OWNER"));

            // When
            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(ResponseCode.VALIDATION_ERROR, response.getBody().getCode());
            assertEquals("User is already a PROJECT_OWNER", response.getBody().getMessage());
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when trying to demote ADMIN")
        void shouldReturn400WhenTryingToDemoteAdmin() {
            // Given
            String userId = "admin-user-id";
            when(promoteToProjectOwnerHandler.handle(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.validationError("ADMIN users cannot be demoted"));

            // When
            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(ResponseCode.VALIDATION_ERROR, response.getBody().getCode());
            assertEquals("ADMIN users cannot be demoted", response.getBody().getMessage());
        }
    }
}
