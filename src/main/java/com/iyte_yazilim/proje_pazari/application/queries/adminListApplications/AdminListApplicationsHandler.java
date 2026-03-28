package com.iyte_yazilim.proje_pazari.application.queries.adminListApplications;

import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationAdminDTO;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminListApplicationsHandler
        implements IRequestHandler<
                AdminListApplicationsQuery, ApiResponse<PagedResponse<ApplicationAdminDTO>>> {

    private final ProjectApplicationRepository applicationRepository;

    @Override
    public ApiResponse<PagedResponse<ApplicationAdminDTO>> handle(
            AdminListApplicationsQuery query) {
        PageRequest pageRequest =
                PageRequest.of(
                        query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProjectApplicationEntity> appPage =
                applicationRepository.findWithFilters(
                        query.status(), query.projectId(), query.userId(), pageRequest);

        List<ApplicationAdminDTO> applications =
                appPage.getContent().stream().map(this::mapToAdminDTO).toList();

        PagedResponse<ApplicationAdminDTO> pagedResponse =
                PagedResponse.<ApplicationAdminDTO>builder()
                        .content(applications)
                        .page(appPage.getNumber())
                        .size(appPage.getSize())
                        .totalElements(appPage.getTotalElements())
                        .totalPages(appPage.getTotalPages())
                        .build();

        return ApiResponse.success(pagedResponse, "Applications retrieved successfully");
    }

    private ApplicationAdminDTO mapToAdminDTO(ProjectApplicationEntity entity) {
        return new ApplicationAdminDTO(
                entity.getId(),
                entity.getProject() != null ? entity.getProject().getId() : null,
                entity.getProject() != null ? entity.getProject().getTitle() : null,
                entity.getUser() != null ? entity.getUser().getId() : null,
                entity.getUser() != null
                        ? entity.getUser().getFirstName() + " " + entity.getUser().getLastName()
                        : null,
                entity.getUser() != null ? entity.getUser().getEmail() : null,
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
