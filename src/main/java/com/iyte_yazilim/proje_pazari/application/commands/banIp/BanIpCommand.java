package com.iyte_yazilim.proje_pazari.application.commands.banIp;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.time.LocalDateTime;

public record BanIpCommand(String ipAddress, String reason, LocalDateTime expiresAt)
        implements IRequest<ApiResponse<Void>> {}
