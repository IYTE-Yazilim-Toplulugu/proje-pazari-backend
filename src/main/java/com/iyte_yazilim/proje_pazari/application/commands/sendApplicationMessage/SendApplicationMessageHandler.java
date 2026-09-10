package com.iyte_yazilim.proje_pazari.application.commands.sendApplicationMessage;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationMessageDto;
import com.iyte_yazilim.proje_pazari.application.services.ApplicationMessageAccessPolicy;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ApplicationMessageRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ApplicationMessageEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SendApplicationMessageHandler
        implements IRequestHandler<
                SendApplicationMessageCommand, ApiResponse<ApplicationMessageDto>> {

    private final ProjectApplicationRepository applicationRepository;
    private final ApplicationMessageRepository messageRepository;
    private final ApplicationMessageAccessPolicy accessPolicy;
    private final MessageService messageService;

    @Override
    @Transactional
    public ApiResponse<ApplicationMessageDto> handle(SendApplicationMessageCommand command) {
        ProjectApplicationEntity application =
                applicationRepository.findById(command.applicationId()).orElse(null);
        if (application == null) {
            return ApiResponse.failure(
                    ErrorCode.APPLICATION_NOT_FOUND,
                    messageService.getMessage("application.not.found"));
        }

        if (!accessPolicy.isParticipant(application, command.senderId())) {
            return ApiResponse.failure(
                    ErrorCode.ACCESS_DENIED, messageService.getMessage("error.forbidden"));
        }

        ApplicationMessageEntity message = new ApplicationMessageEntity();
        message.setApplication(application);
        message.setSender(accessPolicy.participant(application, command.senderId()));
        message.setBody(command.body().trim());

        ApplicationMessageEntity saved = messageRepository.save(message);
        return ApiResponse.created(
                toDto(saved), messageService.getMessage("application.message.sent.success"));
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
