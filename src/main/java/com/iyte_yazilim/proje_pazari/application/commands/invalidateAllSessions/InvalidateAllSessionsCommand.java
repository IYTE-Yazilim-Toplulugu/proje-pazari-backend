package com.iyte_yazilim.proje_pazari.application.commands.invalidateAllSessions;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record InvalidateAllSessionsCommand() implements ICommand<ApiResponse<Void>> {}
