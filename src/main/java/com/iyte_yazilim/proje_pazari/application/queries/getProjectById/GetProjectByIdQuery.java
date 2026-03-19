package com.iyte_yazilim.proje_pazari.application.queries.getProjectById;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record GetProjectByIdQuery(String projectId) implements IRequest<ApiResponse<Project>> {}
