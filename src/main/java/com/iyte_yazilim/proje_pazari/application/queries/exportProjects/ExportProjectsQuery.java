package com.iyte_yazilim.proje_pazari.application.queries.exportProjects;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record ExportProjectsQuery() implements IRequest<ApiResponse<String>> {}
