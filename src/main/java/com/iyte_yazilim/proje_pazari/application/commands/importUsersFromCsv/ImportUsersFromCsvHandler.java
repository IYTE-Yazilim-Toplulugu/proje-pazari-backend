package com.iyte_yazilim.proje_pazari.application.commands.importUsersFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportUsersFromCsvHandler
        implements IRequestHandler<ImportUsersFromCsvCommand, ApiResponse<ImportResultDTO>> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public ApiResponse<ImportResultDTO> handle(ImportUsersFromCsvCommand command) {
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

            // Expected header: email,firstName,lastName,role,password
            String line;
            while ((line = reader.readLine()) != null) {
                totalRows++;
                try {
                    String[] fields = line.split(",", -1);
                    if (fields.length < 5) {
                        errors.add(
                                "Row "
                                        + totalRows
                                        + ": Expected at least 5 fields (email,firstName,lastName,role,password)");
                        continue;
                    }

                    String email = fields[0].trim();
                    String firstName = fields[1].trim();
                    String lastName = fields[2].trim();
                    String roleStr = fields[3].trim();
                    String password = fields[4].trim();

                    if (email.isBlank()) {
                        errors.add("Row " + totalRows + ": Email is required");
                        continue;
                    }

                    if (password.isBlank()) {
                        errors.add("Row " + totalRows + ": Password is required");
                        continue;
                    }

                    RoleType role;
                    try {
                        role = RoleType.valueOf(roleStr.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        errors.add("Row " + totalRows + ": Invalid role: " + roleStr);
                        continue;
                    }

                    if (userRepository.existsByEmail(email)) {
                        errors.add("Row " + totalRows + ": User already exists: " + email);
                        continue;
                    }

                    UserEntity user = new UserEntity();
                    user.setEmail(email);
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setRoles(new HashSet<>(Set.of(role)));
                    user.setPassword(passwordEncoder.encode(password));

                    userRepository.save(user);
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
        return ApiResponse.success(result, "Import completed: " + successCount + " users imported");
    }

    private String resolveCsvContent(ImportUsersFromCsvCommand command) {
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
