package com.iyte_yazilim.proje_pazari.application.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FrontendVerificationLinkBuilderTest {

    @ParameterizedTest
    @MethodSource("urlConfigurations")
    @DisplayName("Should combine frontend URL and verification path without duplicate slashes")
    void shouldBuildConfiguredVerificationUrl(
            String frontendUrl, String verificationPath, String expectedUrl) {
        FrontendVerificationLinkBuilder builder =
                new FrontendVerificationLinkBuilder(frontendUrl, verificationPath);

        assertEquals(expectedUrl, builder.build("verification-token"));
    }

    @Test
    @DisplayName("Should safely encode token as a single query parameter")
    void shouldEncodeTokenAsSingleQueryParameter() {
        FrontendVerificationLinkBuilder builder =
                new FrontendVerificationLinkBuilder("https://projepazari.site/", "/verify-email");

        String url = builder.build("a+b/c==?&%");

        assertEquals("https://projepazari.site/verify-email?token=a%2Bb%2Fc%3D%3D%3F%26%25", url);
        assertEquals(1, countOccurrences(url, "token="));
    }

    private static Stream<Arguments> urlConfigurations() {
        return Stream.of(
                Arguments.of(
                        "https://projepazari.site",
                        "/verify-email",
                        "https://projepazari.site/verify-email?token=verification-token"),
                Arguments.of(
                        "https://projepazari.site/",
                        "/verify-email",
                        "https://projepazari.site/verify-email?token=verification-token"),
                Arguments.of(
                        "https://projepazari.site/",
                        "/account/verify",
                        "https://projepazari.site/account/verify?token=verification-token"),
                Arguments.of(
                        "https://projepazari.site/",
                        "account/verify",
                        "https://projepazari.site/account/verify?token=verification-token"),
                Arguments.of(
                        "https://projepazari.site/",
                        " ",
                        "https://projepazari.site/verify-email?token=verification-token"));
    }

    private int countOccurrences(String value, String target) {
        return value.split(target, -1).length - 1;
    }
}
