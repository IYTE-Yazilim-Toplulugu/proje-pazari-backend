package com.iyte_yazilim.proje_pazari.application.queries.exportUsers;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record ExportUsersQuery() implements IRequest<ApiResponse<String>> {}
