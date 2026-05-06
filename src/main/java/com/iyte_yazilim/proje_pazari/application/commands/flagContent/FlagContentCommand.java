package com.iyte_yazilim.proje_pazari.application.commands.flagContent;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record FlagContentCommand(String contentType, String contentId, String reason)
        implements ICommand<ApiResponse<Void>> {}
