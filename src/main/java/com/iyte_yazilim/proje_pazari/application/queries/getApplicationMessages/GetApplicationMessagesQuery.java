package com.iyte_yazilim.proje_pazari.application.queries.getApplicationMessages;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationMessageDto;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record GetApplicationMessagesQuery(
        @NotBlank(message = "Application ID is required") String applicationId,
        @NotBlank(message = "Requester ID is required") String requesterId,
        @Min(value = 0, message = "Page must not be negative") int page,
        @Min(value = 1, message = "Page size must be at least 1")
                @Max(value = 100, message = "Page size must not exceed 100")
                int size)
        implements IRequest<ApiResponse<PagedResponse<ApplicationMessageDto>>> {}
