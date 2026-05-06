package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record AdminDeleteProjectCommand(String projectId) implements ICommand<ApiResponse<Void>> {}
