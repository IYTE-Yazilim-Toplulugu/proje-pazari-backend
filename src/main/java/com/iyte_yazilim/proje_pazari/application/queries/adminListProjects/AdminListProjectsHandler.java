package com.iyte_yazilim.proje_pazari.application.queries.adminListProjects;

import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectAdminDTO;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminListProjectsHandler
        implements IRequestHandler<
                AdminListProjectsQuery, ApiResponse<PagedResponse<ProjectAdminDTO>>> {

    private final ProjectRepository projectRepository;

    @Override
    public ApiResponse<PagedResponse<ProjectAdminDTO>> handle(AdminListProjectsQuery query) {
        PageRequest pageRequest =
                PageRequest.of(
                        query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProjectEntity> projectPage =
                projectRepository.findWithFilters(
                        query.status(), query.ownerId(), query.search(), pageRequest);

        List<ProjectAdminDTO> projects =
                projectPage.getContent().stream().map(this::mapToAdminDTO).toList();

        PagedResponse<ProjectAdminDTO> pagedResponse =
                PagedResponse.<ProjectAdminDTO>builder()
                        .content(projects)
                        .page(projectPage.getNumber())
                        .size(projectPage.getSize())
                        .totalElements(projectPage.getTotalElements())
                        .totalPages(projectPage.getTotalPages())
                        .build();

        return ApiResponse.success(pagedResponse, "Projects retrieved successfully");
    }

    private ProjectAdminDTO mapToAdminDTO(ProjectEntity entity) {
        return new ProjectAdminDTO(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getSummary(),
                entity.getStatus(),
                entity.getOwner() != null ? entity.getOwner().getId() : null,
                entity.getOwner() != null
                        ? entity.getOwner().getFirstName() + " " + entity.getOwner().getLastName()
                        : null,
                entity.getOwner() != null ? entity.getOwner().getEmail() : null,
                entity.getMaxTeamSize(),
                entity.getCurrentTeamSize(),
                entity.getRequiredSkills(),
                entity.getCategory(),
                entity.getDeadline(),
                entity.isFeatured(),
                entity.getApplications() != null ? entity.getApplications().size() : 0,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
