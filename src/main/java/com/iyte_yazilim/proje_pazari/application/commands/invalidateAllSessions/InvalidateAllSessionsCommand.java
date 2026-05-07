package com.iyte_yazilim.proje_pazari.application.commands.invalidateAllSessions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record InvalidateAllSessionsCommand() implements ICommand<ApiResponse<Void>> {}
