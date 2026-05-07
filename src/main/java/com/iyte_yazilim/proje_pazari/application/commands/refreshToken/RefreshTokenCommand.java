package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record RefreshTokenCommand(String refreshToken)
        implements ICommand<ApiResponse<RefreshTokenResult>> {}
