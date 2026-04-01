package com.iyte_yazilim.proje_pazari.application.commands.invalidateUserSessions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record InvalidateUserSessionsCommand(String userId) implements IRequest<ApiResponse<Void>> {}
