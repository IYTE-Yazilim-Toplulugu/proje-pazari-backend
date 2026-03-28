package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record RefreshTokenCommand(String refreshToken)
        implements IRequest<ApiResponse<RefreshTokenResult>> {}
