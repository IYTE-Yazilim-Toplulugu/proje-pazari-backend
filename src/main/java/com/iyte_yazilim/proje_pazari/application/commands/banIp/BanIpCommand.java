package com.iyte_yazilim.proje_pazari.application.commands.banIp;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import java.time.LocalDateTime;

public record BanIpCommand(String ipAddress, String reason, LocalDateTime expiresAt)
        implements IRequest<ApiResponse<Void>> {}
