package com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record BroadcastEmailCommand(String subject, String body, String targetRole)
        implements ICommand<ApiResponse<Void>> {}
