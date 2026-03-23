package com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications;

import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProjectApplicationsHandler
        implements IRequestHandler<GetProjectApplicationsQuery, ApiResponse<List<ApplicationDto>>> {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    public ApiResponse<List<ApplicationDto>> handle(GetProjectApplicationsQuery query) {

        // --- 1. Verify Project Exists ---
        ProjectEntity projectEntity = projectRepository.findById(query.projectId()).orElse(null);
        if (projectEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.not.found", new Object[] {query.projectId()}));
        }

        // --- 2. Verify Requester is the Project Owner ---
        if (!projectEntity.getOwner().getId().equals(query.requesterId())) {
            return ApiResponse.forbidden(messageService.getMessage("project.owner.mismatch"));
        }

        // --- 3. Fetch Applications ---
        List<ApplicationDto> applications =
                applicationRepository.findByProjectId(query.projectId()).stream()
                        .map(this::toDto)
                        .toList();

        // --- 4. Response ---
        return ApiResponse.success(
                applications, messageService.getMessage("application.list.retrieved.success"));
    }

    private ApplicationDto toDto(ProjectApplicationEntity entity) {
        String applicantName =
                entity.getUser().getFirstName() + " " + entity.getUser().getLastName();
        return new ApplicationDto(
                entity.getId(),
                entity.getProject().getId(),
                entity.getProject().getTitle(),
                entity.getUser().getId(),
                applicantName,
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
