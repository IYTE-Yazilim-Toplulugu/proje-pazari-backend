package com.iyte_yazilim.proje_pazari.application.commands.invalidateUserSessions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record InvalidateUserSessionsCommand(String userId) implements ICommand<ApiResponse<Void>> {}
