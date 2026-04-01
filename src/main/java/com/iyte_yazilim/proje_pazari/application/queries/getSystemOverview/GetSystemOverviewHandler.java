package com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview;

import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetSystemOverviewHandler
        implements IRequestHandler<GetSystemOverviewQuery, ApiResponse<SystemOverviewDTO>> {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;

    @Override
    public ApiResponse<SystemOverviewDTO> handle(GetSystemOverviewQuery query) {
        Map<String, Long> usersByRole = new HashMap<>();
        for (RoleType role : RoleType.values()) {
            usersByRole.put(role.name(), userRepository.countByRole(role));
        }

        SystemOverviewDTO overview =
                SystemOverviewDTO.builder()
                        .totalUsers(userRepository.count())
                        .activeProjects(projectRepository.countByStatus(ProjectStatus.OPEN))
                        .pendingApplications(
                                applicationRepository.countByStatus(ApplicationStatus.PENDING))
                        .newUsersToday(
                                userRepository.countByCreatedAtAfter(
                                        LocalDate.now().atStartOfDay()))
                        .newUsersThisWeek(
                                userRepository.countByCreatedAtAfter(
                                        LocalDate.now().minusWeeks(1).atStartOfDay()))
                        .newUsersThisMonth(
                                userRepository.countByCreatedAtAfter(
                                        LocalDate.now().minusMonths(1).atStartOfDay()))
                        .totalProjects(projectRepository.count())
                        .totalApplications(applicationRepository.count())
                        .usersByRole(usersByRole)
                        .build();

        return ApiResponse.success(overview, "System overview retrieved successfully");
    }
}
