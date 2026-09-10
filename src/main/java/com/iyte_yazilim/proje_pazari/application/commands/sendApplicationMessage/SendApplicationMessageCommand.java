package com.iyte_yazilim.proje_pazari.application.commands.sendApplicationMessage;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.application.common.SensitiveRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationMessageDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SendApplicationMessageCommand(
        @NotBlank(message = "Application ID is required") String applicationId,
        @NotBlank(message = "Sender ID is required") String senderId,
        @NotBlank(message = "Message body is required")
                @Size(max = 2000, message = "Message body must not exceed 2000 characters")
                String body)
        implements ICommand<ApiResponse<ApplicationMessageDto>>, SensitiveRequest {}
