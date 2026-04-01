package com.iyte_yazilim.proje_pazari.application.queries.exportApplications;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record ExportApplicationsQuery() implements IRequest<ApiResponse<String>> {}
