package com.iyte_yazilim.proje_pazari.application.queries.getProject;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;

/** GetProjectQuery */
public record GetProjectQuery(String projectId)
        implements IRequest<ApiResponse<ProjectDetailDto>> {}
