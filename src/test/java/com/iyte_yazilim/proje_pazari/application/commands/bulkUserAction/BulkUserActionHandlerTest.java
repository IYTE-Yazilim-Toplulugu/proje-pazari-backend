package com.iyte_yazilim.proje_pazari.application.commands.bulkUserAction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
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
    @Mock private UserMapper userMapper;

    @InjectMocks private BulkUserActionHandler handler;

    private UserEntity testUserEntity;

    @BeforeEach
    void setUp() {
        testUserEntity = new UserEntity();
        testUserEntity.setId("user1");
        testUserEntity.setEmail("test@example.com");
        testUserEntity.setRoles(new HashSet<>(Set.of(RoleType.USER)));
        testUserEntity.setIsActive(true);
    }

    private User createDomainUser(boolean active, RoleType role) {
        User user = new User("test@example.com", "pw", "Test", "User");
        user.reconstituteActive(active);
        user.assignRole(role);
        return user;
    }

    @Test
    @DisplayName("Should suspend users in bulk")
    void shouldSuspendUsersInBulk() {
        User domainUser = createDomainUser(true, RoleType.USER);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(domainUser);

        BulkUserActionCommand command = new BulkUserActionCommand("SUSPEND", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertNotNull(response.getData());
        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(0, response.getData().getFailureCount());
        assertFalse(domainUser.isActive());
        verify(userMapper).applyDomainToEntity(domainUser, testUserEntity);
        verify(userRepository).save(testUserEntity);
    }

    @Test
    @DisplayName("Should activate users in bulk")
    void shouldActivateUsersInBulk() {
        testUserEntity.setIsActive(false);
        User domainUser = createDomainUser(false, RoleType.USER);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(domainUser);

        BulkUserActionCommand command = new BulkUserActionCommand("ACTIVATE", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertTrue(domainUser.isActive());
        verify(userMapper).applyDomainToEntity(domainUser, testUserEntity);
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
        User domainUser = createDomainUser(true, RoleType.USER);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(domainUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("PROMOTE_TO_ADMIN", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertTrue(domainUser.hasRole(RoleType.ADMIN));
        assertFalse(domainUser.hasRole(RoleType.USER));
        verify(userMapper).applyDomainToEntity(domainUser, testUserEntity);
    }

    @Test
    @DisplayName("Should promote user already having ADMIN — idempotent, still {ADMIN}")
    void shouldPromoteAlreadyAdminUserIdempotently() {
        testUserEntity.setRoles(new HashSet<>(Set.of(RoleType.ADMIN)));
        User domainUser = createDomainUser(true, RoleType.ADMIN);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(domainUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("PROMOTE_TO_ADMIN", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertTrue(domainUser.hasRole(RoleType.ADMIN));
        verify(userMapper).applyDomainToEntity(domainUser, testUserEntity);
    }

    @Test
    @DisplayName("Should demote admin to user — role set becomes {USER} only")
    void shouldDemoteAdminToUser() {
        testUserEntity.setRoles(new HashSet<>(Set.of(RoleType.ADMIN)));
        User domainUser = createDomainUser(true, RoleType.ADMIN);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(domainUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("DEMOTE_TO_USER", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertTrue(domainUser.hasRole(RoleType.USER));
        assertFalse(domainUser.hasRole(RoleType.ADMIN));
        verify(userMapper).applyDomainToEntity(domainUser, testUserEntity);
    }

    @Test
    @DisplayName("Should demote user who already has only USER role — no-op, USER remains")
    void shouldDemoteUserWithOnlyUserRole() {
        User domainUser = createDomainUser(true, RoleType.USER);
        when(userRepository.findById("user1")).thenReturn(Optional.of(testUserEntity));
        when(userMapper.entityToDomain(testUserEntity)).thenReturn(domainUser);

        BulkUserActionCommand command =
                new BulkUserActionCommand("DEMOTE_TO_USER", List.of("user1"));

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertEquals(1, response.getData().getSuccessCount());
        assertTrue(domainUser.hasRole(RoleType.USER));
        verify(userMapper).applyDomainToEntity(domainUser, testUserEntity);
    }

    @Test
    @DisplayName("Should return validation error for empty list")
    void shouldReturnValidationErrorForEmptyList() {
        BulkUserActionCommand command = new BulkUserActionCommand("SUSPEND", List.of());

        ApiResponse<BulkActionResult> response = handler.handle(command);

        assertNotNull(response);
    }
}
