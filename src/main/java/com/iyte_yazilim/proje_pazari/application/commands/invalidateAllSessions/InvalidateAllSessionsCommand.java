package com.iyte_yazilim.proje_pazari.application.commands.invalidateAllSessions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record InvalidateAllSessionsCommand() implements IRequest<ApiResponse<Void>> {}
