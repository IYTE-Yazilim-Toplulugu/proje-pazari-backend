package com.iyte_yazilim.proje_pazari.application.commands.importUsersFromCsv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class ImportUsersFromCsvHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private ImportUsersFromCsvHandler handler;

    @Test
    @DisplayName("Should import valid users from CSV")
    void shouldImportValidUsers() {
        String csv =
                "email,firstName,lastName,role,password\n"
                        + "user1@test.com,John,Doe,APPLICANT,password123\n"
                        + "user2@test.com,Jane,Smith,PROJECT_OWNER,password456\n";

        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(csv));

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(2, response.getData().totalRows());
        assertEquals(2, response.getData().successCount());
        assertEquals(0, response.getData().failedCount());
        assertTrue(response.getData().errors().isEmpty());
        verify(userRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should return validation error for empty CSV content")
    void shouldReturnValidationErrorForEmptyCsv() {
        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(""));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return validation error for null CSV content")
    void shouldReturnValidationErrorForNullCsv() {
        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(null));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should handle rows with missing fields")
    void shouldHandleRowsWithMissingFields() {
        String csv =
                "email,firstName,lastName,role,password\n"
                        + "user1@test.com,John\n"; // Missing fields

        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(csv));

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().totalRows());
        assertEquals(0, response.getData().successCount());
        assertEquals(1, response.getData().failedCount());
        assertFalse(response.getData().errors().isEmpty());
    }

    @Test
    @DisplayName("Should skip duplicate email users")
    void shouldSkipDuplicateEmails() {
        String csv =
                "email,firstName,lastName,role,password\n"
                        + "existing@test.com,John,Doe,APPLICANT,password123\n";

        when(userRepository.existsByEmail("existing@test.com")).thenReturn(true);

        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(csv));

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().totalRows());
        assertEquals(0, response.getData().successCount());
        assertEquals(1, response.getData().failedCount());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle invalid role in CSV")
    void shouldHandleInvalidRole() {
        String csv =
                "email,firstName,lastName,role,password\n"
                        + "user@test.com,John,Doe,INVALID_ROLE,password123\n";

        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(csv));

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(0, response.getData().successCount());
        assertEquals(1, response.getData().failedCount());
    }

    @Test
    @DisplayName("Should handle blank email in CSV row")
    void shouldHandleBlankEmail() {
        String csv =
                "email,firstName,lastName,role,password\n" + " ,John,Doe,APPLICANT,password123\n";

        ApiResponse<ImportResultDTO> response = handler.handle(new ImportUsersFromCsvCommand(csv));

        assertNotNull(response);
        assertEquals(0, response.getData().successCount());
    }
}
