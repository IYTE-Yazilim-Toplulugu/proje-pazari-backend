package com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail;

import com.iyte_yazilim.proje_pazari.application.common.ICommand;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;

public record ResendVerificationEmailCommand(String email) implements ICommand<ApiResponse<Void>> {}
