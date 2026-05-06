package com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record BroadcastEmailCommand(String subject, String body, String targetRole)
        implements ICommand<ApiResponse<Void>> {}
