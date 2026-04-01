package com.iyte_yazilim.proje_pazari.application.queries.getUserProjects;

import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUserProjectsHandler
        implements IRequestHandler<GetUserProjectsQuery, ApiResponse<PagedProjectsResult>> {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "title", "status", "createdAt", "deadline");

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDetailDtoMapper;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedProjectsResult> handle(GetUserProjectsQuery query) {

        // --- 1. Build Pageable ---
        Sort.Direction direction =
                Sort.Direction.DESC.name().equalsIgnoreCase(query.sortDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC;
        String sortBy = SORTABLE_FIELDS.contains(query.sortBy()) ? query.sortBy() : "id";
        Pageable pageable = PageRequest.of(query.page(), query.size(), Sort.by(direction, sortBy));

        // --- 2. Fetch User's Projects via Filter Query ---
        Page<ProjectEntity> projectPage =
                projectRepository.findWithFilters(null, query.userId(), null, pageable);

        // --- 3. Map to DTOs ---
        List<ProjectDetailDto> dtos =
                projectPage.getContent().stream()
                        .map(projectMapper::entityToDomain)
                        .map(projectDetailDtoMapper::domainToDto)
                        .toList();

        // --- 4. Build Result ---
        PagedProjectsResult result =
                new PagedProjectsResult(
                        dtos,
                        projectPage.getNumber(),
                        projectPage.getTotalPages(),
                        projectPage.getTotalElements());

        // --- 5. Response ---
        return ApiResponse.success(result, messageService.getMessage("projects.listed.success"));
    }
}
