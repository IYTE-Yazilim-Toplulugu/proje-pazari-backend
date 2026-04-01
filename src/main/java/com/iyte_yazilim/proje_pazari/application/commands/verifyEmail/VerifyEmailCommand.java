package com.iyte_yazilim.proje_pazari.application.commands.verifyEmail;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.domain.models.results.VerifyEmailResult;
import jakarta.validation.constraints.NotBlank;

public record VerifyEmailCommand(@NotBlank(message = "Token is required") String token)
        implements IRequest<ApiResponse<VerifyEmailResult>> {}
