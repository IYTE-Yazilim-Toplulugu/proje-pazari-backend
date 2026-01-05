package com.iyte_yazilim.proje_pazari.infrastructure.security.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TwoFactorAuthService;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GoogleAuthenticatorTwoFactorService implements TwoFactorAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthenticatorTwoFactorService.class);
    private static final String ISSUER = "IZTECH SOFTWARE SOCIETY";
    private static final int QR_CODE_SIZE = 200;

    private final GoogleAuthenticator googleAuthenticator;

    public GoogleAuthenticatorTwoFactorService() {
        GoogleAuthenticatorConfig config = new GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder()
                .setTimeStepSizeInMillis(30_000)
                .setWindowSize(1)
                .build();
        this.googleAuthenticator = new GoogleAuthenticator(config);
    }

    @Override
    public String generateSecret() {
        final GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        return key.getKey();
    }

    @Override
    public String generateQRCodeUrl(String username, String secret) {
        if (username == null || username.trim().isEmpty()) {
            log.error("Username cannot be null or empty");
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (secret == null || secret.trim().isEmpty()) {
            log.error("Secret cannot be null or empty");
            throw new IllegalArgumentException("Secret cannot be null or empty");
        }

        String url = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                ISSUER, username, new GoogleAuthenticatorKey.Builder(secret).build());

        return generateQRBase64(url);
    }

    @Override
    public String generateQRBase64(String qrCodeText) {
        if (qrCodeText == null || qrCodeText.trim().isEmpty()) {
            log.error("QR code text cannot be null or empty");
            throw new IllegalArgumentException("QR code text cannot be null or empty");
        }

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hintMap = new HashMap<>();
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            BitMatrix bitMatrix = qrCodeWriter.encode(
                    qrCodeText, BarcodeFormat.QR_CODE, QR_CODE_SIZE, QR_CODE_SIZE, hintMap);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(bufferedImage, "png", baos);
                byte[] imageBytes = baos.toByteArray();
                return Base64.getEncoder().encodeToString(imageBytes);
            }
        } catch (WriterException e) {
            log.error("Failed to encode QR code", e);
            throw new RuntimeException("Failed to generate QR code", e);
        } catch (IOException e) {
            log.error("Failed to write QR code image", e);
            throw new RuntimeException("Failed to generate QR code image", e);
        }
    }

    @Override
    public boolean verifyCode(String secret, int code) {
        if (secret == null || secret.trim().isEmpty()) {
            log.error("Secret cannot be null or empty for verification");
            return false;
        }

        return googleAuthenticator.authorize(secret, code);
    }
}
