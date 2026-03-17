package com.iyte_yazilim.proje_pazari.application.commands.uploadFile;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "Command to upload a generic file")
public record UploadFileCommand(
        @Schema(description = "File to upload", requiredMode = Schema.RequiredMode.REQUIRED)
                @NotNull(message = "File is required")
                MultipartFile file)
        implements ICommand<ApiResponse<Map<String, Object>>> {}
