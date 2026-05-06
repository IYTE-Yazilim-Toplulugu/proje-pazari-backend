package com.iyte_yazilim.proje_pazari.application.queries.exportUsers;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record ExportUsersQuery() implements IRequest<ApiResponse<String>> {}
