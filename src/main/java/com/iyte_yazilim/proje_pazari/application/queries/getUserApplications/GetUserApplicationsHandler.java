package com.iyte_yazilim.proje_pazari.application.queries.getUserApplications;

import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserApplicationsHandler
        implements IRequestHandler<GetUserApplicationsQuery, ApiResponse<List<ApplicationDto>>> {

    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    public ApiResponse<List<ApplicationDto>> handle(GetUserApplicationsQuery query) {

        // --- 1. Fetch Applications ---
        List<ApplicationDto> applications =
                applicationRepository.findByUserId(query.userId()).stream()
                        .map(this::toDto)
                        .toList();

        // --- 2. Response ---
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
