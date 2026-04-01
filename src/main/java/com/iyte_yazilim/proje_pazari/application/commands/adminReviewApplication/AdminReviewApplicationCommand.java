package com.iyte_yazilim.proje_pazari.application.commands.adminReviewApplication;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;

public record AdminReviewApplicationCommand(String applicationId, ApplicationStatus status)
        implements IRequest<ApiResponse<Void>> {}
