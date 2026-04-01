package com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record ReviewFlaggedContentCommand(String flagId, String action, String reviewNote)
        implements IRequest<ApiResponse<Void>> {}
