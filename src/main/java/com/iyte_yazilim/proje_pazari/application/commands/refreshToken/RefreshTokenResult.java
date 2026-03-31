package com.iyte_yazilim.proje_pazari.application.commands.refreshToken;

public record RefreshTokenResult(
        String userId, String email, String role, String accessToken, String refreshToken) {}
