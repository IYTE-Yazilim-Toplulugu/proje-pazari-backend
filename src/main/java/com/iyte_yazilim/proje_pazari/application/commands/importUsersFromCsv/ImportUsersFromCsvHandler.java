package com.iyte_yazilim.proje_pazari.application.commands.importUsersFromCsv;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ImportResultDTO;
import com.iyte_yazilim.proje_pazari.application.services.CsvImportService;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVRecord;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImportUsersFromCsvHandler
        implements IRequestHandler<ImportUsersFromCsvCommand, ApiResponse<ImportResultDTO>> {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CsvImportService csvImportService;

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

        CsvImportService.ImportResult<UserEntity> result =
                csvImportService.parse(csvContent, this::mapRow, "users");

        result.successes().forEach(userRepository::save);

        int failedCount = result.totalRows() - result.successes().size();
        ImportResultDTO dto =
                new ImportResultDTO(
                        result.totalRows(),
                        result.successes().size(),
                        failedCount,
                        result.errors());
        return ApiResponse.success(
                dto, "Import completed: " + result.successes().size() + " users imported");
    }

    // Expected header: email,firstName,lastName,role,password
    private UserEntity mapRow(CSVRecord r) {
        String email = r.get(0).trim();
        String firstName = r.get(1).trim();
        String lastName = r.get(2).trim();
        String roleStr = r.get(3).trim();
        String password = r.get(4).trim();

        if (email.isBlank()) throw new IllegalArgumentException("Email is required");
        if (password.isBlank()) throw new IllegalArgumentException("Password is required");

        RoleType role;
        try {
            role = RoleType.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleStr);
        }

        if (userRepository.existsByEmail(email))
            throw new IllegalArgumentException("User already exists: " + email);

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(new HashSet<>(Set.of(role)));
        user.setPassword(passwordEncoder.encode(password));
        return user;
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
