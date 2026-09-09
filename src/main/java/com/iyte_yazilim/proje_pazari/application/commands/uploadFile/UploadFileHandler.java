package com.iyte_yazilim.proje_pazari.application.commands.uploadFile;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileValidationException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UploadFileHandler
        implements IRequestHandler<UploadFileCommand, ApiResponse<Map<String, Object>>> {

    private static final String DEFAULT_DIRECTORY = "uploads";
    private final FileStorageService fileStorageService;

    @Override
    public ApiResponse<Map<String, Object>> handle(UploadFileCommand command) {
        if (command.file() == null || command.file().isEmpty()) {
            throw new FileValidationException("Empty file rejected");
        }

        String filename = command.file().getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.contains("..")) {
            throw new FileValidationException("Invalid filename: " + filename);
        }

        String filePath = fileStorageService.storeFile(command.file(), DEFAULT_DIRECTORY);
        Map<String, Object> fileData =
                Map.of(
                        "filename",
                        filename,
                        "url",
                        "/api/v1/files/" + filePath,
                        "size",
                        command.file().getSize());
        return ApiResponse.success(fileData, "File uploaded successfully");
    }
}
