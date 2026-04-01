package com.iyte_yazilim.proje_pazari.application.commands.flagContent;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record FlagContentCommand(String contentType, String contentId, String reason)
        implements IRequest<ApiResponse<Void>> {}
