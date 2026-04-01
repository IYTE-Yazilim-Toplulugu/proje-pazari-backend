package com.iyte_yazilim.proje_pazari.application.commands.invalidateAllSessions;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record InvalidateAllSessionsCommand() implements IRequest<ApiResponse<Void>> {}
