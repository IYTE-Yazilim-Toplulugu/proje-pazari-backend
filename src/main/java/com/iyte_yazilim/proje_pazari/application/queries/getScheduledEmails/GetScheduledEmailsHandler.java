package com.iyte_yazilim.proje_pazari.application.queries.getScheduledEmails;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.dtos.ScheduledEmailDTO;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetScheduledEmailsHandler
        implements IRequestHandler<GetScheduledEmailsQuery, ApiResponse<List<ScheduledEmailDTO>>> {

    private final ScheduledEmailRepository scheduledEmailRepository;

    @Override
    public ApiResponse<List<ScheduledEmailDTO>> handle(GetScheduledEmailsQuery query) {
        List<ScheduledEmailDTO> emails =
                scheduledEmailRepository.findAll().stream()
                        .map(
                                entity ->
                                        new ScheduledEmailDTO(
                                                entity.getId(),
                                                entity.getSubject(),
                                                entity.getBody(),
                                                entity.getTargetRole(),
                                                entity.getScheduledAt(),
                                                entity.getStatus(),
                                                entity.getCreatedBy(),
                                                entity.getCreatedAt()))
                        .toList();
        return ApiResponse.success(emails, "Scheduled emails retrieved successfully");
    }
}
