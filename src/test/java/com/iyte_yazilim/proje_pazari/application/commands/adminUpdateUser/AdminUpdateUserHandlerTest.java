package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
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
class AdminUpdateUserHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private AdminUpdateUserHandler handler;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("01ABCDEF12345678901234");
        testUser.setEmail("test@example.com");
        testUser.setRole(RoleType.APPLICANT);
        testUser.setIsActive(true);
    }

    @Test
    @DisplayName("Should update user role")
    void shouldUpdateUserRole() {
        when(userRepository.findById("01ABCDEF12345678901234")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand(
                        "01ABCDEF12345678901234", RoleType.PROJECT_OWNER, null, null, null, null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(RoleType.PROJECT_OWNER, testUser.getRole());
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("Should update user active status")
    void shouldUpdateUserActiveStatus() {
        when(userRepository.findById("01ABCDEF12345678901234")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand("01ABCDEF12345678901234", null, false, null, null, null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertFalse(testUser.getIsActive());
    }

    @Test
    @DisplayName("Should return not found for unknown user")
    void shouldReturnNotFoundForUnknownUser() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand("unknown", null, null, null, null, null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        verify(userRepository, never()).save(any());
    }
}
