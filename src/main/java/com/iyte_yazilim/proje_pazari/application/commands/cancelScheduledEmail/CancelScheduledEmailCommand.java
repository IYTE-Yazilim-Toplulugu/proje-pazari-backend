package com.iyte_yazilim.proje_pazari.application.commands.cancelScheduledEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;

public record CancelScheduledEmailCommand(String emailId) implements IRequest<ApiResponse<Void>> {}
