package com.iyte_yazilim.proje_pazari.application.commands.importProjectsFromCsv;

import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportProjectsFromCsvHandler
        implements IRequestHandler<ImportProjectsFromCsvCommand, ApiResponse<ImportResultDTO>> {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

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

        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int successCount = 0;

        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                return ApiResponse.validationError("CSV file is empty");
            }

            // Expected header:
            // title,description,ownerEmail,status,maxTeamSize,category,requiredSkills
            String line;
            while ((line = reader.readLine()) != null) {
                totalRows++;
                try {
                    String[] fields = line.split(",", -1);
                    if (fields.length < 4) {
                        errors.add(
                                "Row "
                                        + totalRows
                                        + ": Expected at least 4 fields (title,description,ownerEmail,status)");
                        continue;
                    }

                    String title = fields[0].trim();
                    String description = fields[1].trim();
                    String ownerEmail = fields[2].trim();
                    String statusStr = fields[3].trim();

                    if (title.isBlank()) {
                        errors.add("Row " + totalRows + ": Title is required");
                        continue;
                    }

                    if (ownerEmail.isBlank()) {
                        errors.add("Row " + totalRows + ": Owner email is required");
                        continue;
                    }

                    Optional<UserEntity> ownerOpt = userRepository.findByEmail(ownerEmail);
                    if (ownerOpt.isEmpty()) {
                        errors.add("Row " + totalRows + ": Owner not found: " + ownerEmail);
                        continue;
                    }

                    ProjectStatus status;
                    try {
                        status =
                                statusStr.isBlank()
                                        ? ProjectStatus.DRAFT
                                        : ProjectStatus.valueOf(statusStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Row " + totalRows + ": Invalid status: " + statusStr);
                        continue;
                    }

                    ProjectEntity project = new ProjectEntity();
                    project.setTitle(title);
                    project.setDescription(description);
                    project.setOwner(ownerOpt.get());
                    project.setStatus(status);

                    if (fields.length > 4 && !fields[4].trim().isBlank()) {
                        try {
                            project.setMaxTeamSize(Integer.parseInt(fields[4].trim()));
                        } catch (NumberFormatException ignored) {
                        }
                    }

                    if (fields.length > 5 && !fields[5].trim().isBlank()) {
                        project.setCategory(fields[5].trim());
                    }

                    if (fields.length > 6 && !fields[6].trim().isBlank()) {
                        List<String> skills =
                                Arrays.stream(fields[6].trim().split(";"))
                                        .map(String::trim)
                                        .filter(s -> !s.isBlank())
                                        .toList();
                        project.setRequiredSkills(skills);
                    }

                    projectRepository.save(project);
                    successCount++;
                } catch (Exception e) {
                    errors.add("Row " + totalRows + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("Failed to parse CSV", e);
            return ApiResponse.validationError("Failed to parse CSV: " + e.getMessage());
        }

        int failedCount = totalRows - successCount;
        ImportResultDTO result = new ImportResultDTO(totalRows, successCount, failedCount, errors);
        return ApiResponse.success(
                result, "Import completed: " + successCount + " projects imported");
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
