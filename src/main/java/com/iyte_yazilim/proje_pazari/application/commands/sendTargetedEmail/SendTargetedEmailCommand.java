package com.iyte_yazilim.proje_pazari.application.commands.sendTargetedEmail;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import java.util.List;

public record SendTargetedEmailCommand(List<String> userIds, String subject, String body)
        implements IRequest<ApiResponse<Void>> {}
