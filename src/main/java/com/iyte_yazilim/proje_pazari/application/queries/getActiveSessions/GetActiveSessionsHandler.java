package com.iyte_yazilim.proje_pazari.application.queries.getActiveSessions;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ActiveSessionDTO;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.RefreshTokenEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetActiveSessionsHandler
        implements IRequestHandler<GetActiveSessionsQuery, ApiResponse<List<ActiveSessionDTO>>> {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<ActiveSessionDTO>> handle(GetActiveSessionsQuery query) {
        List<RefreshTokenEntity> activeTokens =
                refreshTokenRepository.findAll().stream()
                        .filter(t -> !t.getRevoked() && t.getExpiresAt().isAfter(Instant.now()))
                        .toList();

        Map<String, List<RefreshTokenEntity>> groupedByUser =
                activeTokens.stream().collect(Collectors.groupingBy(RefreshTokenEntity::getUserId));

        List<ActiveSessionDTO> sessions =
                groupedByUser.entrySet().stream()
                        .map(
                                entry -> {
                                    String userId = entry.getKey();
                                    List<RefreshTokenEntity> tokens = entry.getValue();

                                    Optional<UserEntity> user = userRepository.findById(userId);
                                    String email = user.map(UserEntity::getEmail).orElse("unknown");

                                    Instant lastActivity =
                                            tokens.stream()
                                                    .map(RefreshTokenEntity::getCreatedAt)
                                                    .max(Comparator.naturalOrder())
                                                    .orElse(Instant.now());

                                    return new ActiveSessionDTO(
                                            userId, email, tokens.size(), lastActivity);
                                })
                        .toList();

        return ApiResponse.success(sessions, "Active sessions retrieved successfully");
    }
}
