package com.iyte_yazilim.proje_pazari.application.queries.getUserApplications;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedApplicationsResult;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles user application queries with pagination and optional status filtering.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
 * @since 2026-03-23
 */
@Service
@RequiredArgsConstructor
public class GetUserApplicationsHandler
        implements IRequestHandler<GetUserApplicationsQuery, ApiResponse<PagedApplicationsResult>> {

    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedApplicationsResult> handle(GetUserApplicationsQuery query) {

        // --- 1. Construct Pageable ---
        Pageable pageable =
                PageRequest.of(
                        query.page(), query.size(), Sort.by(Sort.Direction.DESC, "createdAt"));

        // --- 2. Fetch Paged Applications with Optional Status Filter ---
        Page<ProjectApplicationEntity> applicationPage =
                applicationRepository.findWithFilters(
                        query.status(), null, query.userId(), pageable);

        // --- 3. Map to DTOs ---
        List<ApplicationDto> applications =
                applicationPage.getContent().stream().map(this::toDto).toList();

        // --- 4. Construct Paged Result ---
        PagedApplicationsResult pagedResult =
                new PagedApplicationsResult(
                        applications,
                        applicationPage.getNumber(),
                        applicationPage.getTotalPages(),
                        applicationPage.getTotalElements());

        // --- 5. Response ---
        return ApiResponse.success(
                pagedResult, messageService.getMessage("application.list.retrieved.success"));
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
                entity.getReviewMessage(),
                entity.getCreatedAt());
    }
}
