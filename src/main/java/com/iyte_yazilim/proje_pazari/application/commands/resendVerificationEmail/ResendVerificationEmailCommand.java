package com.iyte_yazilim.proje_pazari.application.commands.resendVerificationEmail;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;

public record ResendVerificationEmailCommand(String email) implements IRequest<ApiResponse<Void>> {}
