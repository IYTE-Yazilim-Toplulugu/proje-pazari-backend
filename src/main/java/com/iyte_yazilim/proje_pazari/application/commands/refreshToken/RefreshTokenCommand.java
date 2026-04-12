package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record RefreshTokenCommand(String refreshToken)
        implements ICommand<ApiResponse<RefreshTokenResult>> {}
