package com.iyte_yazilim.proje_pazari.application.queries.exportApplications;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record ExportApplicationsQuery() implements IRequest<ApiResponse<String>> {}
