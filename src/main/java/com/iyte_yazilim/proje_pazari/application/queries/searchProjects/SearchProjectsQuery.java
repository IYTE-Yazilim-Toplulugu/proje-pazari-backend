package com.iyte_yazilim.proje_pazari.application.queries.searchProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SearchProjectsQuery(
        @NotBlank @Size(min = 2, max = 100) String q,
        String status,
        List<String> tags,
        @Min(0) int page,
        @Min(1) @Max(100) int size)
        implements IRequest<ApiResponse<PagedProjectsResult>> {}
