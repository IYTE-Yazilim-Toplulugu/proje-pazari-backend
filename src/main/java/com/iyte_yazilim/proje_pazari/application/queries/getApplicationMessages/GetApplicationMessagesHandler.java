package com.iyte_yazilim.proje_pazari.application.queries.getApplicationMessages;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationMessageDto;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import com.iyte_yazilim.proje_pazari.application.services.ApplicationMessageAccessPolicy;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ApplicationMessageRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ApplicationMessageEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class GetApplicationMessagesHandler
        implements IRequestHandler<
                GetApplicationMessagesQuery, ApiResponse<PagedResponse<ApplicationMessageDto>>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ApplicationMessageRepository messageRepository;
    private final ApplicationMessageAccessPolicy accessPolicy;
    private final MessageService messageService;

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<PagedResponse<ApplicationMessageDto>> handle(
            GetApplicationMessagesQuery query) {
        ProjectApplicationEntity application =
                applicationRepository.findById(query.applicationId()).orElse(null);
        if (application == null) {
            return ApiResponse.failure(
                    ErrorCode.APPLICATION_NOT_FOUND,
                    messageService.getMessage("application.not.found"));
        }

        if (!accessPolicy.isParticipant(application, query.requesterId())) {
            return ApiResponse.failure(
                    ErrorCode.ACCESS_DENIED, messageService.getMessage("error.forbidden"));
        }

        Sort chronologicalOrder = Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id"));
        Page<ApplicationMessageEntity> page =
                messageRepository.findByApplication_Id(
                        query.applicationId(),
                        PageRequest.of(query.page(), query.size(), chronologicalOrder));
        List<ApplicationMessageDto> messages = page.getContent().stream().map(this::toDto).toList();
        PagedResponse<ApplicationMessageDto> response =
                new PagedResponse<>(
                        messages,
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages());

        return ApiResponse.success(
                response, messageService.getMessage("application.message.list.retrieved.success"));
    }

    private ApplicationMessageDto toDto(ApplicationMessageEntity message) {
        return new ApplicationMessageDto(
                message.getId(),
                message.getApplication().getId(),
                message.getSender().getId(),
                message.getBody(),
                message.getCreatedAt());
    }
}
