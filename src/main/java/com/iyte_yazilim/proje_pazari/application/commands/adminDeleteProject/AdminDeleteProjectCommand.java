package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record AdminDeleteProjectCommand(String projectId) implements IRequest<ApiResponse<Void>> {}
