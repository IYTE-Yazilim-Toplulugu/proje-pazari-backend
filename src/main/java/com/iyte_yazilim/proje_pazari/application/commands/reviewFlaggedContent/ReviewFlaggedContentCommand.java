package com.iyte_yazilim.proje_pazari.application.commands.reviewFlaggedContent;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record ReviewFlaggedContentCommand(String flagId, String action, String reviewNote)
        implements ICommand<ApiResponse<Void>> {}
