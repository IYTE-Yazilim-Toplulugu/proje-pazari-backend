package com.iyte_yazilim.proje_pazari.application.queries.adminListUsers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.UserAdminDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class AdminListUsersHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private AdminListUsersHandler handler;

    private UserEntity testUser;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("01ABCDEF12345678901234");
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRoles(new HashSet<>(Set.of(RoleType.USER)));
        testUser.setIsActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should list users with pagination")
    void shouldListUsersWithPagination() {
        Object[] row = new Object[] {testUser, 2L, 3L};
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row));
        when(userRepository.findWithFiltersAndCounts(any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        AdminListUsersQuery query = new AdminListUsersQuery(0, 50, null, null, null);
        ApiResponse<PagedResponse<UserAdminDTO>> response = handler.handle(query);

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().getContent().size());
        UserAdminDTO dto = response.getData().getContent().get(0);
        assertEquals("test@example.com", dto.email());
        assertEquals(2, dto.projectCount());
        assertEquals(3, dto.applicationCount());
    }

    @Test
    @DisplayName("Should filter users by role")
    void shouldFilterByRole() {
        Object[] row = new Object[] {testUser, 0L, 0L};
        Page<Object[]> page = new PageImpl<>(Collections.singletonList(row));
        when(userRepository.findWithFiltersAndCounts(
                        eq(RoleType.USER), any(), any(), any(Pageable.class)))
                .thenReturn(page);

        AdminListUsersQuery query = new AdminListUsersQuery(0, 50, RoleType.USER, null, null);
        ApiResponse<PagedResponse<UserAdminDTO>> response = handler.handle(query);

        assertNotNull(response.getData());
        verify(userRepository)
                .findWithFiltersAndCounts(eq(RoleType.USER), any(), any(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return empty page when no users")
    void shouldReturnEmptyPage() {
        Page<Object[]> emptyPage = new PageImpl<>(List.of());
        when(userRepository.findWithFiltersAndCounts(any(), any(), any(), any(Pageable.class)))
                .thenReturn(emptyPage);

        AdminListUsersQuery query = new AdminListUsersQuery(0, 50, null, null, null);
        ApiResponse<PagedResponse<UserAdminDTO>> response = handler.handle(query);

        assertNotNull(response.getData());
        assertTrue(response.getData().getContent().isEmpty());
    }
}
