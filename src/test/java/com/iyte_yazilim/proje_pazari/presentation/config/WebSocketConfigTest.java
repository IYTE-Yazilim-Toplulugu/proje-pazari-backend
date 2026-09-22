package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.iyte_yazilim.proje_pazari.presentation.security.WebSocketOutboundSecurityInterceptor;
import com.iyte_yazilim.proje_pazari.presentation.security.WebSocketSecurityInterceptor;
import java.util.List;
import org.junit.jupiter.api.Test;

class WebSocketConfigTest {

    private final WebSocketSecurityInterceptor inbound = mock(WebSocketSecurityInterceptor.class);
    private final WebSocketOutboundSecurityInterceptor outbound =
            mock(WebSocketOutboundSecurityInterceptor.class);

    @Test
    void acceptsExplicitOrigins() {
        assertDoesNotThrow(
                () ->
                        new WebSocketConfig(
                                inbound,
                                outbound,
                                List.of("https://app.example", " https://admin.example ")));
    }

    @Test
    void rejectsEmptyOriginConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WebSocketConfig(inbound, outbound, List.of(" ")));
    }

    @Test
    void rejectsWildcardOriginConfiguration() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new WebSocketConfig(inbound, outbound, List.of("https://*.example")));
    }
}
