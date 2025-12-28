package com.iyte_yazilim.proje_pazari.application.dtos;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
}
