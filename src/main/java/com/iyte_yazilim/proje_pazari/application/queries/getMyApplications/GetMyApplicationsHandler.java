package com.iyte_yazilim.proje_pazari.application.queries.getMyApplications;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.domain.models.results.MyApplicationResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetMyApplicationsHandler
        implements IRequestHandler<GetMyApplicationsQuery, ApiResponse<List<MyApplicationResult>>> {

    private final ProjectApplicationRepository applicationRepository;
    private final MessageService messageService;

    @Override
    public ApiResponse<List<MyApplicationResult>> handle(GetMyApplicationsQuery query) {
        List<MyApplicationResult> results =
                applicationRepository.findByUserId(query.userId()).stream()
                        .map(this::toResult)
                        .toList();

        return ApiResponse.success(
                results, messageService.getMessage("application.list.retrieved.success"));
    }

    private MyApplicationResult toResult(ProjectApplicationEntity entity) {
        return new MyApplicationResult(
                entity.getId(),
                entity.getProject().getId(),
                entity.getProject().getTitle(),
                entity.getStatus().toString(),
                entity.getCreatedAt());
    }
}
