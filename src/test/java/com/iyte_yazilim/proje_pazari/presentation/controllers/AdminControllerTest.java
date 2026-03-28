package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.commands.adminDeleteUser.AdminDeleteUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction.BulkUserActionCommand;
import com.iyte_yazilim.proje_pazari.application.commands.flagContent.FlagContentCommand;
import com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig.UpdateSystemConfigCommand;
import com.iyte_yazilim.proje_pazari.application.common.IMediator;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.StorageHealthDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemConfigDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemHealthDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.application.queries.adminListUsers.AdminListUsersQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemConfig.GetSystemConfigQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemHealth.GetSystemHealthQuery;
import com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview.GetSystemOverviewQuery;
import com.iyte_yazilim.proje_pazari.application.services.StorageHealthService;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.presentation.mappers.IRequestMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private IMediator mediator;

    @Mock private IRequestMapper requestMapper;

    @Mock private StorageHealthService storageHealthService;

    @InjectMocks private AdminController adminController;

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

            ResponseEntity<ApiResponse<BulkActionResult>> response =
                    adminController.bulkUserAction(
                            new AdminController.BulkUserActionRequest(
                                    "SUSPEND", List.of("user-1", "user-2")));

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
                    adminController.flagContent(
                            "PROJECT", "proj-1", new AdminController.FlagContentRequest("SPAM"));

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
    @DisplayName("getStorageHealth() method")
    class GetStorageHealthTests {

        @Test
        @DisplayName("should return storage health response")
        void shouldReturnStorageHealthResponse() {
            StorageHealthDTO health =
                    new StorageHealthDTO(
                            "MinioStorageAdapter",
                            true,
                            null,
                            1024L,
                            List.of("proje-pazari-files", "proje-pazari-avatars"),
                            java.time.LocalDateTime.now());
            when(storageHealthService.getStorageHealth()).thenReturn(health);

            ResponseEntity<ApiResponse<StorageHealthDTO>> response =
                    adminController.getStorageHealth();

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getBody().getData().available());
            assertEquals(1024L, response.getBody().getData().usedSpaceBytes());
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
                    adminController.updateSystemConfig(
                            new AdminController.UpdateSystemConfigRequest(Map.of("key", "value")));

            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
    }
}
