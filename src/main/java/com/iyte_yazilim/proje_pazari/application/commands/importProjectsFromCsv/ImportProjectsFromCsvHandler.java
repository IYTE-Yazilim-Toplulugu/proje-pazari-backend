package com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.application.services.CsvImportService;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImportProjectsFromCsvHandler
        implements IRequestHandler<ImportProjectsFromCsvCommand, ApiResponse<ImportResultDTO>> {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final CsvImportService csvImportService;

    @Override
    @Transactional
    public ApiResponse<ImportResultDTO> handle(ImportProjectsFromCsvCommand command) {
        String csvContent;
        try {
            csvContent = resolveCsvContent(command);
        } catch (IllegalArgumentException e) {
            return ApiResponse.validationError(e.getMessage());
        }

        if (csvContent == null || csvContent.isBlank()) {
            return ApiResponse.validationError("CSV content is empty");
        }

        CsvImportService.ImportResult<ProjectEntity> result =
                csvImportService.parse(csvContent, this::mapRow, "projects");

        result.successes().forEach(projectRepository::save);

        int failedCount = result.totalRows() - result.successes().size();
        ImportResultDTO dto =
                new ImportResultDTO(
                        result.totalRows(),
                        result.successes().size(),
                        failedCount,
                        result.errors());
        return ApiResponse.success(
                dto, "Import completed: " + result.successes().size() + " projects imported");
    }

    // Expected header: title,description,ownerEmail,status,maxTeamSize,category,requiredSkills
    private ProjectEntity mapRow(CSVRecord r) {
        String title = r.get(0).trim();
        String description = r.get(1).trim();
        String ownerEmail = r.get(2).trim();
        String statusStr = r.get(3).trim();

        if (title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (ownerEmail.isBlank()) throw new IllegalArgumentException("Owner email is required");

        Optional<UserEntity> ownerOpt = userRepository.findByEmail(ownerEmail);
        if (ownerOpt.isEmpty())
            throw new IllegalArgumentException("Owner not found: " + ownerEmail);

        ProjectStatus status;
        try {
            status =
                    statusStr.isBlank()
                            ? ProjectStatus.DRAFT
                            : ProjectStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr);
        }

        ProjectEntity project = new ProjectEntity();
        project.setTitle(title);
        project.setDescription(description);
        project.setOwner(ownerOpt.get());
        project.setStatus(status);

        if (r.size() > 4 && !r.get(4).trim().isBlank()) {
            try {
                project.setMaxTeamSize(Integer.parseInt(r.get(4).trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        if (r.size() > 5 && !r.get(5).trim().isBlank()) {
            project.setCategory(r.get(5).trim());
        }
        if (r.size() > 6 && !r.get(6).trim().isBlank()) {
            List<String> skills =
                    Arrays.stream(r.get(6).trim().split(";"))
                            .map(String::trim)
                            .filter(s -> !s.isBlank())
                            .toList();
            project.setRequiredSkills(skills);
        }
        return project;
    }

    private String resolveCsvContent(ImportProjectsFromCsvCommand command) {
        if (command.csvContent() != null && !command.csvContent().isBlank()) {
            return command.csvContent();
        }
        if (command.file() == null || command.file().isEmpty()) {
            return null;
        }
        try {
            return new String(command.file().getBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read file: " + e.getMessage(), e);
        }
    }
}
