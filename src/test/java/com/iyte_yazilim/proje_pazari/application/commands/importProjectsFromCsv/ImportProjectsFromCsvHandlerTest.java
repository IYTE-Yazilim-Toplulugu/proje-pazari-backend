package com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.application.services.CsvImportService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImportProjectsFromCsvHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private UserRepository userRepository;
    @Spy private CsvImportService csvImportService = new CsvImportService();

    @InjectMocks private ImportProjectsFromCsvHandler handler;

    @Test
    @DisplayName("Should import valid projects from CSV")
    void shouldImportValidProjects() {
        String csv =
                "title,description,ownerEmail,status,maxTeamSize,category,requiredSkills\n"
                        + "Project A,A test project,owner@test.com,DRAFT,5,Software,Java;Spring\n";

        UserEntity owner = new UserEntity();
        owner.setId("owner-1");
        owner.setEmail("owner@test.com");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(projectRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApiResponse<ImportResultDTO> response =
                handler.handle(new ImportProjectsFromCsvCommand(csv));

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(1, response.getData().totalRows());
        assertEquals(1, response.getData().successCount());
        assertEquals(0, response.getData().failedCount());
        verify(projectRepository).save(any());
    }

    @Test
    @DisplayName("Should return validation error for empty CSV")
    void shouldReturnValidationErrorForEmptyCsv() {
        ApiResponse<ImportResultDTO> response =
                handler.handle(new ImportProjectsFromCsvCommand(""));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should handle owner not found")
    void shouldHandleOwnerNotFound() {
        String csv =
                "title,description,ownerEmail,status\n" + "Project A,Desc,unknown@test.com,DRAFT\n";

        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        ApiResponse<ImportResultDTO> response =
                handler.handle(new ImportProjectsFromCsvCommand(csv));

        assertNotNull(response);
        assertEquals(0, response.getData().successCount());
        assertEquals(1, response.getData().failedCount());
    }

    @Test
    @DisplayName("Should handle rows with insufficient fields")
    void shouldHandleInsufficientFields() {
        String csv = "title,description,ownerEmail,status\n" + "Project A,Desc\n"; // Only 2 fields

        ApiResponse<ImportResultDTO> response =
                handler.handle(new ImportProjectsFromCsvCommand(csv));

        assertNotNull(response);
        assertEquals(0, response.getData().successCount());
        assertEquals(1, response.getData().failedCount());
    }

    @Test
    @DisplayName("Should handle invalid project status")
    void shouldHandleInvalidStatus() {
        String csv =
                "title,description,ownerEmail,status\n"
                        + "Project A,Desc,owner@test.com,INVALID_STATUS\n";

        UserEntity owner = new UserEntity();
        owner.setId("owner-1");
        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));

        ApiResponse<ImportResultDTO> response =
                handler.handle(new ImportProjectsFromCsvCommand(csv));

        assertNotNull(response);
        assertEquals(0, response.getData().successCount());
        assertEquals(1, response.getData().failedCount());
    }
}
