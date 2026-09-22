package com.iyte_yazilim.proje_pazari.application.common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Guards the {@link ErrorCode} contract: every constant must carry a category and a message key
 * that actually resolves to a non-blank, user-safe message in both supported locales. This catches
 * the common mistake of adding an {@code ErrorCode} without its message-bundle entries.
 */
class ErrorCodeTest {

    private static final Properties TR = load("/messages.properties");
    private static final Properties EN = load("/messages_en.properties");

    private static Properties load(String resource) {
        Properties props = new Properties();
        try (InputStream in = ErrorCodeTest.class.getResourceAsStream(resource)) {
            assertNotNull(in, "Missing message bundle: " + resource);
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AssertionError("Failed to load " + resource, e);
        }
        return props;
    }

    @ParameterizedTest
    @EnumSource(ErrorCode.class)
    @DisplayName("Every ErrorCode has a category and a non-blank message key in TR + EN bundles")
    void errorCode_resolvesMessageKey_inBothLocales(ErrorCode errorCode) {
        assertNotNull(errorCode.getCategory(), errorCode + " must have a category");

        String key = errorCode.getMessageKey();
        assertNotNull(key, errorCode + " must have a message key");

        String tr = TR.getProperty(key);
        String en = EN.getProperty(key);
        assertTrue(
                tr != null && !tr.isBlank(),
                errorCode + " -> '" + key + "' missing or blank in messages.properties");
        assertTrue(
                en != null && !en.isBlank(),
                errorCode + " -> '" + key + "' missing or blank in messages_en.properties");
    }
}
