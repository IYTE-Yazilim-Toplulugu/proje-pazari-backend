package com.iyte_yazilim.proje_pazari.application.commands.banIp;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.BannedIpEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BanIpHandler implements IRequestHandler<BanIpCommand, ApiResponse<Void>> {

    private final BannedIpRepository bannedIpRepository;

    @Override
    public ApiResponse<Void> handle(BanIpCommand command) {
        if (command.ipAddress() == null || command.ipAddress().isBlank()) {
            return ApiResponse.validationError("IP address is required");
        }

        if (bannedIpRepository.existsByIpAddress(command.ipAddress())) {
            return ApiResponse.conflict("IP address is already banned: " + command.ipAddress());
        }

        String currentUser = "SYSTEM";
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                currentUser = auth.getName();
            }
        } catch (Exception ignored) {
        }

        BannedIpEntity ban =
                BannedIpEntity.builder()
                        .ipAddress(command.ipAddress())
                        .reason(command.reason())
                        .bannedBy(currentUser)
                        .expiresAt(command.expiresAt())
                        .build();

        bannedIpRepository.save(ban);
        return ApiResponse.success(null, "IP address banned: " + command.ipAddress());
    }
}
