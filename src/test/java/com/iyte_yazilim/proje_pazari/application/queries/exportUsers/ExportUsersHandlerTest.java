package com.iyte_yazilim.proje_pazari.application.queries.exportUsers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExportUsersHandlerTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private ExportUsersHandler handler;

    @Test
    @DisplayName("Should export users as CSV with header row")
    void shouldExportUsersAsCsv() {
        UserEntity user = new UserEntity();
        user.setId("user-1");
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRoles(new HashSet<>(Set.of(RoleType.USER)));
        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        user.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 0, 0));

        when(userRepository.findAll()).thenReturn(List.of(user));

        ApiResponse<String> response = handler.handle(new ExportUsersQuery());

        assertNotNull(response.getData());
        String csv = response.getData();

        // Check header
        assertTrue(csv.startsWith("id,email,firstName,lastName,role,isActive,createdAt,updatedAt"));

        // Check data row
        assertTrue(csv.contains("user-1"));
        assertTrue(csv.contains("test@example.com"));
        assertTrue(csv.contains("Test"));
        assertTrue(csv.contains("USER"));
    }

    @Test
    @DisplayName("Should export empty CSV with only header when no users")
    void shouldExportEmptyCsvWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(List.of());

        ApiResponse<String> response = handler.handle(new ExportUsersQuery());

        assertNotNull(response.getData());
        String csv = response.getData();
        assertTrue(csv.startsWith("id,email,firstName,lastName,role,isActive,createdAt,updatedAt"));
        // Only header row, no data rows
        assertEquals(1, csv.strip().split("\n").length);
    }

    @Test
    @DisplayName("Should handle null fields in CSV export")
    void shouldHandleNullFieldsInCsvExport() {
        UserEntity user = new UserEntity();
        user.setId("user-2");
        user.setEmail("minimal@example.com");
        // firstName, lastName, role, isActive, createdAt, updatedAt are null

        when(userRepository.findAll()).thenReturn(List.of(user));

        ApiResponse<String> response = handler.handle(new ExportUsersQuery());

        assertNotNull(response.getData());
        assertTrue(response.getData().contains("user-2"));
        assertTrue(response.getData().contains("minimal@example.com"));
    }
}
