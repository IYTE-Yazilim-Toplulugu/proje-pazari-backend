package com.iyte_yazilim.proje_pazari.application.commands.uploadFile;

import com.iyte_yazilim.proje_pazari.application.services.FileStorageService;
import com.iyte_yazilim.proje_pazari.domain.exceptions.FileStorageException;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
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
            return ApiResponse.badRequest("File is empty");
        }

        String filename = command.file().getOriginalFilename();
        if (filename == null || filename.isBlank() || filename.contains("..")) {
            return ApiResponse.badRequest("Invalid filename");
        }

        try {
            String filePath = fileStorageService.storeFile(command.file(), DEFAULT_DIRECTORY);
            Map<String, Object> fileData =
                    Map.of(
                            "filename", filename,
                            "url", "/api/v1/files/" + filePath,
                            "size", command.file().getSize());
            return ApiResponse.success(fileData, "File uploaded successfully");
        } catch (FileStorageException e) {
            log.error("File upload failed: {}", e.getMessage());
            return ApiResponse.error("File upload failed: " + e.getMessage());
        }
    }
}
