package com.iyte_yazilim.proje_pazari.application.commands.deleteProject;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import jakarta.validation.constraints.NotBlank;

public record DeleteProjectCommand(
        @NotBlank(message = "Project ID is required") String projectId,
        @NotBlank(message = "Owner ID is required") String ownerId)
        implements ICommand<ApiResponse<Void>> {}
