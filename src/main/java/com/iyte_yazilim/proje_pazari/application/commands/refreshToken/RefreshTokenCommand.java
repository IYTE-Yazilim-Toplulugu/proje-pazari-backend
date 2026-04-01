package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record RefreshTokenCommand(String refreshToken)
        implements IRequest<ApiResponse<RefreshTokenResult>> {}
