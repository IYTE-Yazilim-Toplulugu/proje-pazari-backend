package com.iyte_yazilim.proje_pazari.application.commands.verifyEmail;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailCommand(@NotBlank(message = "Token is required") String token) {}
