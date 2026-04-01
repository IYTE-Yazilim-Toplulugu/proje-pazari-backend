package com.iyte_yazilim.proje_pazari.application.queries.getProject;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

/** GetProjectQuery */
public record GetProjectQuery(String projectId)
        implements IRequest<ApiResponse<ProjectDetailDto>> {}
