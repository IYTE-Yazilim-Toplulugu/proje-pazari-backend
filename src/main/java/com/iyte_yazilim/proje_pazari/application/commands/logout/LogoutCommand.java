package com.iyte_yazilim.proje_pazari.application.commands.logout;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record LogoutCommand(String accessToken, String refreshToken, String userId)
        implements IRequest<ApiResponse<Void>> {}
