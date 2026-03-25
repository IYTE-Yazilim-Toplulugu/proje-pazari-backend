package com.iyte_yazilim.proje_pazari.application.queries.getMyApplications;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.MyApplicationResult;
import java.util.List;

public record GetMyApplicationsQuery(String userId)
        implements IRequest<ApiResponse<List<MyApplicationResult>>> {}
