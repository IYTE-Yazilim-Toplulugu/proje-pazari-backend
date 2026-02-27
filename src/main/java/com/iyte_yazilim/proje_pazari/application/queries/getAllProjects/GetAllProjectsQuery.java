package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.PagedProjectsResult;

public record GetAllProjectsQuery(int page, int size, String sortBy, String sortDirection)
        implements IRequest<ApiResponse<PagedProjectsResult>> {
}
