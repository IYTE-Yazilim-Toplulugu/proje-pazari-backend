package com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record GetProjectApplicationsQuery(String projectId, String requesterId)
        implements IRequest<ApiResponse<List<ApplicationDto>>> {}
