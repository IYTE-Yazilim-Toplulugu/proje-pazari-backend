package com.iyte_yazilim.proje_pazari.application.commands.invalidateUserSessions;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record InvalidateUserSessionsCommand(String userId) implements ICommand<ApiResponse<Void>> {}
