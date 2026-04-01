package com.iyte_yazilim.proje_pazari.application.commands.unbanIp;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UnbanIpHandler implements IRequestHandler<UnbanIpCommand, ApiResponse<Void>> {

    private final BannedIpRepository bannedIpRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(UnbanIpCommand command) {
        if (command.ipAddress() == null || command.ipAddress().isBlank()) {
            return ApiResponse.validationError("IP address is required");
        }

        if (!bannedIpRepository.existsByIpAddress(command.ipAddress())) {
            return ApiResponse.notFound("IP address is not banned: " + command.ipAddress());
        }

        bannedIpRepository.deleteByIpAddress(command.ipAddress());
        return ApiResponse.success(null, "IP address unbanned: " + command.ipAddress());
    }
}
