package com.iyte_yazilim.proje_pazari.application.queries.getUserProjects;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record GetUserProjectsQuery(
        String userId, int page, int size, String sortBy, String sortDirection)
        implements IRequest<ApiResponse<PagedProjectsResult>> {}
