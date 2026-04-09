package com.iyte_yazilim.proje_pazari.application.queries.getAnalyticsTrends;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.AnalyticsTrendsDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetAnalyticsTrendsHandler
        implements IRequestHandler<GetAnalyticsTrendsQuery, ApiResponse<AnalyticsTrendsDTO>> {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;

    @Override
    public ApiResponse<AnalyticsTrendsDTO> handle(GetAnalyticsTrendsQuery query) {
        int days = Math.max(1, Math.min(query.days(), 365));
        LocalDate today = LocalDate.now();

        List<AnalyticsTrendsDTO.DailyDataPoint> dataPoints = new ArrayList<>();

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            long newUsers = userRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long newProjects = projectRepository.countByCreatedAtBetween(startOfDay, endOfDay);
            long newApplications =
                    applicationRepository.countByCreatedAtBetween(startOfDay, endOfDay);

            dataPoints.add(
                    new AnalyticsTrendsDTO.DailyDataPoint(
                            date, newUsers, newProjects, newApplications));
        }

        AnalyticsTrendsDTO trends = new AnalyticsTrendsDTO(dataPoints, days);
        return ApiResponse.success(trends, "Analytics trends retrieved successfully");
    }
}
