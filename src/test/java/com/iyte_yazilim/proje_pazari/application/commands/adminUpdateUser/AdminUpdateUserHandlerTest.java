package com.iyte_yazilim.proje_pazari.application.commands.adminUpdateUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.UserNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
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
    @Mock private UserMapper userMapper;

    @InjectMocks private AdminUpdateUserHandler handler;

    private UserEntity testUserEntity;
    private User testDomainUser;

    @BeforeEach
    void setUp() {
        testUserEntity = new UserEntity();
        testUserEntity.setId("01ABCDEF12345678901234");
        testUserEntity.setEmail("test@example.com");
        testUserEntity.setRoles(new HashSet<>(Set.of(RoleType.USER)));
        testUserEntity.setIsActive(true);

        testDomainUser = new User("test@example.com", "pw", "Test", "User");
        testDomainUser.assignRole(RoleType.USER);
    }

    @Test
    @DisplayName("Should update user role")
    void shouldUpdateUserRole() {
        when(userRepository.findById("01ABCDEF12345678901234"))
                .thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(testDomainUser);

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand(
                        "01ABCDEF12345678901234", RoleType.ADMIN, null, null, null, null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(testDomainUser.hasRole(RoleType.ADMIN));
        assertFalse(testDomainUser.hasRole(RoleType.USER));
        verify(userMapper).applyDomainToEntity(testDomainUser, testUserEntity);
        verify(userRepository).save(testUserEntity);
    }

    @Test
    @DisplayName("Should update user active status")
    void shouldUpdateUserActiveStatus() {
        when(userRepository.findById("01ABCDEF12345678901234"))
                .thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(testDomainUser);

        AdminUpdateUserCommand command =
                new AdminUpdateUserCommand("01ABCDEF12345678901234", null, false, null, null, null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertFalse(testDomainUser.isActive());
        verify(userMapper).applyDomainToEntity(testDomainUser, testUserEntity);
        verify(userRepository).save(testUserEntity);
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
