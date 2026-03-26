package com.iyte_yazilim.proje_pazari.application.commands.deleteProject;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Command to delete a project")
public record DeleteProjectCommand(
        @Schema(hidden = true) String projectId, @Schema(hidden = true) String ownerId)
        implements ICommand<ApiResponse<Void>> {}
