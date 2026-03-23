package com.iyte_yazilim.proje_pazari.application.queries.getUserApplications;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedApplicationsResult;

public record GetUserApplicationsQuery(String userId, int page, int size, ApplicationStatus status)
        implements IRequest<ApiResponse<PagedApplicationsResult>> {}
