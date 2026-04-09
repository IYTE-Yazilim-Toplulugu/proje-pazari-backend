package com.iyte_yazilim.proje_pazari.application.queries.getApplicationStats;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationStatsDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetApplicationStatsHandler
        implements IRequestHandler<GetApplicationStatsQuery, ApiResponse<ApplicationStatsDTO>> {

    private final ProjectApplicationRepository applicationRepository;

    @Override
    public ApiResponse<ApplicationStatsDTO> handle(GetApplicationStatsQuery query) {
        Map<String, Long> applicationsByStatus = new HashMap<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            applicationsByStatus.put(status.name(), applicationRepository.countByStatus(status));
        }

        long total = applicationRepository.count();
        long approved = applicationRepository.countByStatus(ApplicationStatus.APPROVED);
        double acceptanceRate = total > 0 ? (double) approved / total * 100.0 : 0.0;

        ApplicationStatsDTO stats =
                ApplicationStatsDTO.builder()
                        .totalApplications(total)
                        .applicationsByStatus(applicationsByStatus)
                        .acceptanceRate(acceptanceRate)
                        .build();

        return ApiResponse.success(stats, "Application statistics retrieved successfully");
    }
}
