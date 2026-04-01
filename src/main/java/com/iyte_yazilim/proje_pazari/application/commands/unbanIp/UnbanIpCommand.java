package com.iyte_yazilim.proje_pazari.application.commands.unbanIp;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record UnbanIpCommand(String ipAddress) implements IRequest<ApiResponse<Void>> {}
