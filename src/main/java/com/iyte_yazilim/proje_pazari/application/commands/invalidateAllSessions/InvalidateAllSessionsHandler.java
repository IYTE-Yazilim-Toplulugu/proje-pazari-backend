package com.iyte_yazilim.proje_pazari.application.commands.invalidateAllSessions;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvalidateAllSessionsHandler
        implements IRequestHandler<InvalidateAllSessionsCommand, ApiResponse<Void>> {

    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    @Transactional
    public ApiResponse<Void> handle(InvalidateAllSessionsCommand command) {
        long count = refreshTokenRepository.count();
        refreshTokenRepository.deleteAll();
        log.info("Invalidated all {} sessions", count);
        return ApiResponse.success(null, "All " + count + " sessions invalidated successfully");
    }
}
