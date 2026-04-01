package com.iyte_yazilim.proje_pazari.application.queries.listBannedIps;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.BannedIpDTO;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.BannedIpRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ListBannedIpsHandler
        implements IRequestHandler<ListBannedIpsQuery, ApiResponse<List<BannedIpDTO>>> {

    private final BannedIpRepository bannedIpRepository;

    @Override
    public ApiResponse<List<BannedIpDTO>> handle(ListBannedIpsQuery query) {
        List<BannedIpDTO> bannedIps =
                bannedIpRepository.findAll().stream()
                        .map(
                                entity ->
                                        new BannedIpDTO(
                                                entity.getId(),
                                                entity.getIpAddress(),
                                                entity.getReason(),
                                                entity.getBannedBy(),
                                                entity.getBannedAt(),
                                                entity.getExpiresAt()))
                        .toList();

        return ApiResponse.success(bannedIps, "Banned IPs retrieved successfully");
    }
}
