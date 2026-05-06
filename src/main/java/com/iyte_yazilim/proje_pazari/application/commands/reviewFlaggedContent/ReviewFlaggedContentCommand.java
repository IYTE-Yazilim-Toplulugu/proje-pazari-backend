package com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record ReviewFlaggedContentCommand(String flagId, String action, String reviewNote)
        implements ICommand<ApiResponse<Void>> {}
