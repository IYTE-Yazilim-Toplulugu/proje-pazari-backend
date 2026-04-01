package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.HashSet;
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
class AdminUpdateUserHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private AdminUpdateUserHandler handler;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("01ABCDEF12345678901234");
        testUser.setEmail("test@example.com");
        testUser.setRoles(new HashSet<>(Set.of(RoleType.USER)));
        testUser.setIsActive(true);
    }

    @Test
    @DisplayName("Should update user role")
    void shouldUpdateUserRole() {
        when(userRepository.findById("01ABCDEF12345678901234")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any())).thenReturn(testUser);

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand(
                        "01ABCDEF12345678901234", RoleType.ADMIN, null, null, null, null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(Set.of(RoleType.ADMIN), testUser.getRoles());
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
    @DisplayName("Should throw UserNotFoundException for unknown user")
    void shouldThrowException_whenUserNotFound() {
        when(userRepository.findById("unknown")).thenReturn(Optional.empty());

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand("unknown", null, null, null, null, null);

        assertThrows(UserNotFoundException.class, () -> handler.handle(command));
        verify(userRepository, never()).save(any());
    }
}
