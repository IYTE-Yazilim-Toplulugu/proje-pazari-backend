package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser.AdminDeleteUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction.BulkUserActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.flagContent.FlagContentCommand;
import com.iyte_yazilim.proje_pazari.application.commands.promoteToProjectOwner.PromoteToProjectOwnerCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig.UpdateSystemConfigCommand;
import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemConfigDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.application.queries.adminListUsers.AdminListUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig.GetSystemConfigQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth.GetSystemHealthQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview.GetSystemOverviewQuery;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.presentation.mappers.IRequestMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private IMediator mediator;

    @Mock private IRequestMapper requestMapper;

    @InjectMocks private AdminController adminController;

    @Nested
    @DisplayName("promoteToProjectOwner() method")
    class PromoteToProjectOwnerTests {

        @Test
        @DisplayName("should return 200 OK when promotion succeeds")
        void shouldReturn200WhenPromotionSucceeds() {
            String userId = "target-user-id";
            when(mediator.send(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(
                            ApiResponse.success(
                                    null, "User promoted to PROJECT_OWNER successfully"));

            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(ResponseCode.SUCCESS, response.getBody().getCode());
            assertEquals(
                    "User promoted to PROJECT_OWNER successfully", response.getBody().getMessage());
        }

        @Test
        @DisplayName("should pass correct userId to handler")
        void shouldPassCorrectUserIdToHandler() {
            String userId = "specific-user-id";
            when(mediator.send(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.success(null, "Success"));

            ArgumentCaptor<PromoteToProjectOwnerCommand> captor =
                    ArgumentCaptor.forClass(PromoteToProjectOwnerCommand.class);

            adminController.promoteToProjectOwner(userId);

            verify(mediator).send(captor.capture());
            assertEquals(userId, captor.getValue().userId());
        }

        @Test
        @DisplayName("should return 404 NOT_FOUND when user does not exist")
        void shouldReturn404WhenUserNotFound() {
            String userId = "nonexistent-user";
            when(mediator.send(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.notFound("User not found with id: nonexistent-user"));

            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertEquals(ResponseCode.NOT_FOUND, response.getBody().getCode());
            assertEquals(
                    "User not found with id: nonexistent-user", response.getBody().getMessage());
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when user is already PROJECT_OWNER")
        void shouldReturn400WhenAlreadyProjectOwner() {
            String userId = "project-owner-id";
            when(mediator.send(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.validationError("User is already a PROJECT_OWNER"));

            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(ResponseCode.VALIDATION_ERROR, response.getBody().getCode());
            assertEquals("User is already a PROJECT_OWNER", response.getBody().getMessage());
        }

        @Test
        @DisplayName("should return 400 BAD_REQUEST when trying to demote ADMIN")
        void shouldReturn400WhenTryingToDemoteAdmin() {
            String userId = "admin-user-id";
            when(mediator.send(any(PromoteToProjectOwnerCommand.class)))
                    .thenReturn(ApiResponse.validationError("ADMIN users cannot be demoted"));

            ResponseEntity<ApiResponse<Void>> response =
                    adminController.promoteToProjectOwner(userId);

            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(ResponseCode.VALIDATION_ERROR, response.getBody().getCode());
            assertEquals("ADMIN users cannot be demoted", response.getBody().getMessage());
        }
    }

    @Nested
    @DisplayName("listUsers() method")
    class ListUsersTests {

        @Test
        @DisplayName("should return 200 with paginated user list")
        void shouldReturn200WithPaginatedUsers() {
            PagedResponse<UserAdminDTO> pagedResponse = new PagedResponse<>(List.of(), 0, 50, 0, 0);
            when(mediator.send(any(AdminListUsersQuery.class)))
                    .thenReturn(ApiResponse.success(pagedResponse, "Users listed"));

            ResponseEntity<ApiResponse<PagedResponse<UserAdminDTO>>> response =
                    adminController.listUsers(0, 50, null, null, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody().getData());
        }
    }

    @Nested
    @DisplayName("deleteUser() method")
    class DeleteUserTests {

        @Test
        @DisplayName("should return 200 when user deleted successfully")
        void shouldReturn200WhenUserDeleted() {
            when(mediator.send(any(AdminDeleteUserCommand.class)))
                    .thenReturn(ApiResponse.success(null, "User deactivated"));

            ResponseEntity<ApiResponse<Void>> response = adminController.deleteUser("user-1");

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }

        @Test
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() {
            when(mediator.send(any(AdminDeleteUserCommand.class)))
                    .thenReturn(ApiResponse.notFound("User not found"));

            ResponseEntity<ApiResponse<Void>> response = adminController.deleteUser("unknown");

            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("bulkUserAction() method")
    class BulkUserActionTests {

        @Test
        @DisplayName("should return bulk action result")
        void shouldReturnBulkActionResult() {
            BulkActionResult result = new BulkActionResult();
            result.incrementSuccess();
            when(mediator.send(any(BulkUserActionCommand.class)))
                    .thenReturn(ApiResponse.success(result, "Bulk action completed"));

            Map<String, Object> request =
                    Map.of("action", "SUSPEND", "userIds", List.of("user-1", "user-2"));
            ResponseEntity<ApiResponse<BulkActionResult>> response =
                    adminController.bulkUserAction(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(1, response.getBody().getData().getSuccessCount());
        }
    }

    @Nested
    @DisplayName("getOverview() method")
    class GetOverviewTests {

        @Test
        @DisplayName("should return system overview statistics")
        void shouldReturnSystemOverview() {
            SystemOverviewDTO overview =
                    SystemOverviewDTO.builder()
                            .totalUsers(100)
                            .activeProjects(15)
                            .pendingApplications(25)
                            .build();
            when(mediator.send(any(GetSystemOverviewQuery.class)))
                    .thenReturn(ApiResponse.success(overview, "Stats retrieved"));

            ResponseEntity<ApiResponse<SystemOverviewDTO>> response = adminController.getOverview();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(100, response.getBody().getData().getTotalUsers());
        }
    }

    @Nested
    @DisplayName("flagContent() method")
    class FlagContentTests {

        @Test
        @DisplayName("should return 200 when content flagged successfully")
        void shouldReturn200WhenContentFlagged() {
            when(mediator.send(any(FlagContentCommand.class)))
                    .thenReturn(ApiResponse.success(null, "Content flagged successfully"));

            ResponseEntity<ApiResponse<Void>> response =
                    adminController.flagContent("PROJECT", "proj-1", Map.of("reason", "SPAM"));

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }

    @Nested
    @DisplayName("getSystemHealth() method")
    class GetSystemHealthTests {

        @Test
        @DisplayName("should return system health status")
        void shouldReturnSystemHealth() {
            SystemHealthDTO health =
                    new SystemHealthDTO(
                            "HEALTHY",
                            Map.of("database", "UP"),
                            Map.of("totalMB", 512L),
                            "0d 1h 30m 0s");
            when(mediator.send(any(GetSystemHealthQuery.class)))
                    .thenReturn(ApiResponse.success(health, "Health retrieved"));

            ResponseEntity<ApiResponse<SystemHealthDTO>> response =
                    adminController.getSystemHealth();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("HEALTHY", response.getBody().getData().status());
        }
    }

    @Nested
    @DisplayName("getSystemConfig() method")
    class GetSystemConfigTests {

        @Test
        @DisplayName("should return system configuration")
        void shouldReturnSystemConfig() {
            SystemConfigDTO config =
                    new SystemConfigDTO(
                            Map.of("maintenanceMode", "false", "registrationEnabled", "true"));
            when(mediator.send(any(GetSystemConfigQuery.class)))
                    .thenReturn(ApiResponse.success(config, "Config retrieved"));

            ResponseEntity<ApiResponse<SystemConfigDTO>> response =
                    adminController.getSystemConfig();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("false", response.getBody().getData().configs().get("maintenanceMode"));
        }
    }

    @Nested
    @DisplayName("updateSystemConfig() method")
    class UpdateSystemConfigTests {

        @Test
        @DisplayName("should return 200 when config updated")
        void shouldReturn200WhenConfigUpdated() {
            when(mediator.send(any(UpdateSystemConfigCommand.class)))
                    .thenReturn(ApiResponse.success(null, "Config updated"));

            ResponseEntity<ApiResponse<Void>> response =
                    adminController.updateSystemConfig(Map.of("key", "value"));

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}
