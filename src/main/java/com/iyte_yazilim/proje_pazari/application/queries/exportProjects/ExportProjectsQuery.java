package com.iyte_yazilim.proje_pazari.application.queries.exportProjects;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record ExportProjectsQuery() implements IRequest<ApiResponse<String>> {}
