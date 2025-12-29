package com.iyte_yazilim.proje_pazari.domain.models.results;

import com.github.f4b6a3.ulid.Ulid;

public record VerifyEmailResult(
    Ulid userId,
    String email,
    String message
) {}
