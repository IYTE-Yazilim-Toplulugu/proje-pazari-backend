package com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.ApplicationSummaryResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetProjectApplicationsHandler
        implements IRequestHandler<
                GetProjectApplicationsQuery, ApiResponse<List<ApplicationSummaryResult>>> {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    public ApiResponse<List<ApplicationSummaryResult>> handle(GetProjectApplicationsQuery query) {

        ProjectEntity project = projectRepository.findById(query.projectId()).orElse(null);
        if (project == null) {
            return ApiResponse.notFound(messageService.getMessage("project.not.found"));
        }

        if (!project.getOwner().getId().equals(query.ownerId())) {
            return ApiResponse.forbidden(messageService.getMessage("error.forbidden"));
        }

        List<ApplicationSummaryResult> results =
                applicationRepository.findByProjectId(query.projectId()).stream()
                        .map(this::toSummary)
                        .toList();

        return ApiResponse.success(
                results, messageService.getMessage("application.list.retrieved.success"));
    }

    private ApplicationSummaryResult toSummary(ProjectApplicationEntity entity) {
        return new ApplicationSummaryResult(
                entity.getId(),
                entity.getUser().getId(),
                entity.getUser().getEmail(),
                entity.getUser().getFirstName(),
                entity.getUser().getLastName(),
                entity.getStatus().toString(),
                entity.getCreatedAt());
    }
}
