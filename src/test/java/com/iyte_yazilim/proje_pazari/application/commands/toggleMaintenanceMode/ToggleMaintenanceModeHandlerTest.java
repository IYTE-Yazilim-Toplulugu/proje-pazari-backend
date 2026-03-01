package com.iyte_yazilim.proje_pazari.application.commands.toggleMaintenanceMode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ToggleMaintenanceModeHandlerTest {

    @Mock private SystemConfigRepository systemConfigRepository;

    @InjectMocks private ToggleMaintenanceModeHandler handler;

    @Test
    @DisplayName("Should enable maintenance mode by creating new config")
    void shouldEnableMaintenanceModeNewConfig() {
        when(systemConfigRepository.findByConfigKey("maintenanceMode"))
                .thenReturn(Optional.empty());
        when(systemConfigRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ApiResponse<Void> response = handler.handle(new ToggleMaintenanceModeCommand(true));

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertTrue(response.getMessage().contains("enabled"));
        verify(systemConfigRepository).save(any(SystemConfigEntity.class));
    }

    @Test
    @DisplayName("Should disable maintenance mode by updating existing config")
    void shouldDisableMaintenanceModeExistingConfig() {
        SystemConfigEntity existing =
                SystemConfigEntity.builder()
                        .id("config-1")
                        .configKey("maintenanceMode")
                        .configValue("true")
                        .build();
        when(systemConfigRepository.findByConfigKey("maintenanceMode"))
                .thenReturn(Optional.of(existing));
        when(systemConfigRepository.save(any())).thenReturn(existing);

        ApiResponse<Void> response = handler.handle(new ToggleMaintenanceModeCommand(false));

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertTrue(response.getMessage().contains("disabled"));
        assertEquals("false", existing.getConfigValue());
        verify(systemConfigRepository).save(existing);
    }

    @Test
    @DisplayName("Should update existing config when enabling maintenance mode")
    void shouldUpdateExistingConfigWhenEnabling() {
        SystemConfigEntity existing =
                SystemConfigEntity.builder()
                        .id("config-1")
                        .configKey("maintenanceMode")
                        .configValue("false")
                        .build();
        when(systemConfigRepository.findByConfigKey("maintenanceMode"))
                .thenReturn(Optional.of(existing));
        when(systemConfigRepository.save(any())).thenReturn(existing);

        ApiResponse<Void> response = handler.handle(new ToggleMaintenanceModeCommand(true));

        assertNotNull(response);
        assertEquals("true", existing.getConfigValue());
    }
}
