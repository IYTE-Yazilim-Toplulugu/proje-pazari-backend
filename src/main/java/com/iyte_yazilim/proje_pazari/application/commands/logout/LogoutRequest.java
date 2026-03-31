package com.iyte_yazilim.proje_pazari.application.commands.logout;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank String refreshToken) {}
