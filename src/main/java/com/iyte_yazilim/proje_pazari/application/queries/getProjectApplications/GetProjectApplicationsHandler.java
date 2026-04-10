package com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles project application queries with optional status filtering.
 *
 * <p>Only the project owner can view applications for their project. When a status filter is
 * provided, only applications matching that status are returned.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
 * @since 2026-03-23
 */
@Service
@RequiredArgsConstructor
public class GetProjectApplicationsHandler
        implements IRequestHandler<GetProjectApplicationsQuery, ApiResponse<List<ApplicationDto>>> {

    private final ProjectRepository projectRepository;
    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
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

        // --- 3. Fetch Applications with Optional Status Filter ---
        List<ApplicationDto> applications =
                applicationRepository
                        .findByProjectIdWithOptionalStatus(query.projectId(), query.status())
                        .stream()
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
                entity.getUser().getEmail(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
