package com.iyte_yazilim.proje_pazari.application.queries.getProjectStats;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectStatsDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProjectStatsHandler
        implements IRequestHandler<GetProjectStatsQuery, ApiResponse<ProjectStatsDTO>> {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;

    @Override
    public ApiResponse<ProjectStatsDTO> handle(GetProjectStatsQuery query) {
        Map<String, Long> projectsByStatus = new HashMap<>();
        for (ProjectStatus status : ProjectStatus.values()) {
            projectsByStatus.put(status.name(), projectRepository.countByStatus(status));
        }

        Map<String, Long> projectsByCategory = new HashMap<>();
        List<ProjectEntity> allProjects = projectRepository.findAll();
        for (ProjectEntity project : allProjects) {
            String category =
                    project.getCategory() != null ? project.getCategory() : "UNCATEGORIZED";
            projectsByCategory.merge(category, 1L, Long::sum);
        }

        long totalProjects = projectRepository.count();
        long totalApplications = applicationRepository.count();
        double avgApps = totalProjects > 0 ? (double) totalApplications / totalProjects : 0.0;

        ProjectStatsDTO stats =
                ProjectStatsDTO.builder()
                        .totalProjects(totalProjects)
                        .projectsByStatus(projectsByStatus)
                        .projectsByCategory(projectsByCategory)
                        .avgApplicationsPerProject(avgApps)
                        .build();

        return ApiResponse.success(stats, "Project statistics retrieved successfully");
    }
}
