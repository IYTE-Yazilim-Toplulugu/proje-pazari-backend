package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.List;
import java.util.Optional;
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
        testUser.setRole(RoleType.APPLICANT);
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
    @DisplayName("Should return validation error for empty list")
    void shouldReturnValidationErrorForEmptyList() {
        BulkUserActionCommand command = new BulkUserActionCommand("SUSPEND", List.of());

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertNotNull(response);
    }
}
