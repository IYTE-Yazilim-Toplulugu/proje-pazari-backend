package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;

public record GetAllProjectsQuery(int page, int size, String sortBy, String sortDirection)
        implements IRequest<ApiResponse<PagedProjectsResult>> {}
