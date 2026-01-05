package com.iyte_yazilim.proje_pazari.domain.interfaces;

public interface TwoFactorAuthService {
    String generateSecret();

    String generateQRCodeUrl(String username, String secret);

    boolean verifyCode(String secret, int code);

    String generateQRBase64(String url);
}
