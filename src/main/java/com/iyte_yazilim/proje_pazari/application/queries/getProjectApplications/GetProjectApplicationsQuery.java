package com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.ApplicationSummaryResult;
import java.util.List;

public record GetProjectApplicationsQuery(String projectId, String ownerId)
        implements IRequest<ApiResponse<List<ApplicationSummaryResult>>> {}
