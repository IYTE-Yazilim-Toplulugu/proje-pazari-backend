package com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ICommand;

public record ResendVerificationEmailCommand(String email) implements ICommand<ApiResponse<Void>> {}
