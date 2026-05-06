package com.iyte_yazilim.proje_pazari.application.queries.suggestProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record SuggestProjectsQuery(@NotBlank @Size(min = 1, max = 100) String q)
        implements IRequest<ApiResponse<List<String>>> {}
