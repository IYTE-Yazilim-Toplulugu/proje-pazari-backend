package com.iyte_yazilim.proje_pazari.application.commands.unbanIp;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record UnbanIpCommand(String ipAddress) implements IRequest<ApiResponse<Void>> {}
