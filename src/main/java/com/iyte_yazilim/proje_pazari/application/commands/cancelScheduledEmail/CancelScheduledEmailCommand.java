package com.iyte_yazilim.proje_pazari.application.commands.cancelScheduledEmail;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record CancelScheduledEmailCommand(String emailId) implements ICommand<ApiResponse<Void>> {}
