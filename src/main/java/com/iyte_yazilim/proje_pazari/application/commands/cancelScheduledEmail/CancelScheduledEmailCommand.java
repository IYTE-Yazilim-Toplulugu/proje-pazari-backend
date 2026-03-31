package com.iyte_yazilim.proje_pazari.application.commands.cancelScheduledEmail;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record CancelScheduledEmailCommand(String emailId) implements IRequest<ApiResponse<Void>> {}
