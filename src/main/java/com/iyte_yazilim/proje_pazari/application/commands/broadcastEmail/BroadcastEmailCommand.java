package com.iyte_yazilim.proje_pazari.application.commands.broadcastEmail;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record BroadcastEmailCommand(String subject, String body, String targetRole)
        implements IRequest<ApiResponse<Void>> {}
