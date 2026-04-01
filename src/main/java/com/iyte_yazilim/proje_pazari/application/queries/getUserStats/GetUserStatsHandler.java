package com.iyte_yazilim.proje_pazari.application.queries.getUserStats;

import com.iyte_yazilim.proje_pazari.application.dtos.UserStatsDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetUserStatsHandler
        implements IRequestHandler<GetUserStatsQuery, ApiResponse<UserStatsDTO>> {

    private final UserRepository userRepository;

    @Override
    public ApiResponse<UserStatsDTO> handle(GetUserStatsQuery query) {
        Map<String, Long> usersByRole = new HashMap<>();
        for (RoleType role : RoleType.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }

        Map<String, Long> usersByEmailDomain = new HashMap<>();
        List<UserEntity> allUsers = userRepository.findAll();
        for (UserEntity user : allUsers) {
            String domain = user.getEmail().substring(user.getEmail().indexOf('@'));
            usersByEmailDomain.merge(domain, 1L, Long::sum);
        }

        UserStatsDTO stats =
                UserStatsDTO.builder()
                        .totalUsers(userRepository.count())
                        .activeUsers(userRepository.countByIsActive(true))
                        .inactiveUsers(userRepository.countByIsActive(false))
                        .usersByRole(usersByRole)
                        .usersByEmailDomain(usersByEmailDomain)
                        .build();

        return ApiResponse.success(stats, "User statistics retrieved successfully");
    }
}
