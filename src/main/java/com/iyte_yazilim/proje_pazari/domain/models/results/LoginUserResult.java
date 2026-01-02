package com.iyte_yazilim.proje_pazari.domain.models.results;

public record LoginUserResult(
        String userId,
        String email,
        String firstName,
        String lastName,
        String role,
        String token) {}