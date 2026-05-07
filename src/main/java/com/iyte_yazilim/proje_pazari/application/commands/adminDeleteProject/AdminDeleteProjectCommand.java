package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record AdminDeleteProjectCommand(String projectId) implements ICommand<ApiResponse<Void>> {}
