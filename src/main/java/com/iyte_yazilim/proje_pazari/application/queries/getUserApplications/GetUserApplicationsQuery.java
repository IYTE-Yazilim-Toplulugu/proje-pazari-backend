package com.iyte_yazilim.proje_pazari.application.queries.getUserApplications;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record GetUserApplicationsQuery(String userId)
        implements IRequest<ApiResponse<List<ApplicationDto>>> {}
