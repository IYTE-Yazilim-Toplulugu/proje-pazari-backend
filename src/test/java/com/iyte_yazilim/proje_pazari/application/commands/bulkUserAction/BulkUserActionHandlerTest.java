package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkUserActionHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private BulkUserActionHandler handler;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user1");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>(Set.of(RoleType.USER)));
        testUser.setIsActive(true);
    }

    @Test
    @DisplayName("Should suspend users in bulk")
    void shouldSuspendUsersInBulk() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        BulkUserActionCommand command = new BulkUserActionCommand("SUSPEND", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertNotNull(response.getData());
        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(0, response.getData().getFailureCount());
        assertFalse(testUser.getIsActive());
    }

    @Test
    @DisplayName("Should activate users in bulk")
    void shouldActivateUsersInBulk() {
        testUser.setIsActive(false);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        BulkUserActionCommand command = new BulkUserActionCommand("ACTIVATE", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertTrue(testUser.getIsActive());
    }

    @Test
    @DisplayName("Should handle not found users in bulk")
    void shouldHandleNotFoundInBulk() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        BulkUserActionCommand command = new BulkUserActionCommand("SUSPEND", List.of("unknown"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(0, response.getData().getSuccessCount());
        assertEquals(1, response.getData().getFailureCount());
    }

    @Test
    @DisplayName("Should promote user to admin — role set becomes {ADMIN} only")
    void shouldPromoteUserToAdmin() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("PROMOTE_TO_ADMIN", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(Set.of(RoleType.ADMIN), testUser.getRoles());
    }

    @Test
    @DisplayName("Should promote user already having ADMIN — idempotent, still {ADMIN}")
    void shouldPromoteAlreadyAdminUserIdempotently() {
        testUser.setRoles(new HashSet<>(Set.of(RoleType.ADMIN)));
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("PROMOTE_TO_ADMIN", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(Set.of(RoleType.ADMIN), testUser.getRoles());
    }

    @Test
    @DisplayName("Should demote admin to user — role set becomes {USER} only")
    void shouldDemoteAdminToUser() {
        testUser.setRoles(new HashSet<>(Set.of(RoleType.ADMIN)));
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("DEMOTE_TO_USER", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(Set.of(RoleType.USER), testUser.getRoles());
    }

    @Test
    @DisplayName("Should demote user who already has only USER role — no-op, USER remains")
    void shouldDemoteUserWithOnlyUserRole() {
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("DEMOTE_TO_USER", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(Set.of(RoleType.USER), testUser.getRoles());
    }

    @Test
    @DisplayName("Should return validation error for empty list")
    void shouldReturnValidationErrorForEmptyList() {
        BulkUserActionCommand command = new BulkUserActionCommand("SUSPEND", List.of());

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertNotNull(response);
    }
}
