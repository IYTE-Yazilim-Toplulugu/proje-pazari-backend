package com.iyte_yazilim.proje_pazari.application.commands.updateSystemConfig;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.SystemConfigRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateSystemConfigHandlerTest {

    @Mock private SystemConfigRepository systemConfigRepository;

    @InjectMocks private UpdateSystemConfigHandler handler;

    @Test
    @DisplayName("Should create new config entry when key does not exist")
    void shouldCreateNewConfigEntry() {
        when(systemConfigRepository.findByConfigKey("newKey")).thenReturn(Optional.empty());

        UpdateSystemConfigCommand command =
                new UpdateSystemConfigCommand(Map.of("newKey", "newValue"));

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("1 entries"));

        ArgumentCaptor<SystemConfigEntity> captor =
                ArgumentCaptor.forClass(SystemConfigEntity.class);
        verify(systemConfigRepository).save(captor.capture());
        assertEquals("newKey", captor.getValue().getConfigKey());
        assertEquals("newValue", captor.getValue().getConfigValue());
    }

    @Test
    @DisplayName("Should update existing config entry")
    void shouldUpdateExistingConfigEntry() {
        SystemConfigEntity existing =
                SystemConfigEntity.builder()
                        .id("config-1")
                        .configKey("existingKey")
                        .configValue("oldValue")
                        .build();
        when(systemConfigRepository.findByConfigKey("existingKey"))
                .thenReturn(Optional.of(existing));

        UpdateSystemConfigCommand command =
                new UpdateSystemConfigCommand(Map.of("existingKey", "updatedValue"));

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals("updatedValue", existing.getConfigValue());
        verify(systemConfigRepository).save(existing);
    }

    @Test
    @DisplayName("Should return validation error for empty configs")
    void shouldReturnValidationErrorForEmptyConfigs() {
        UpdateSystemConfigCommand command = new UpdateSystemConfigCommand(Map.of());

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("empty"));
        verify(systemConfigRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for null configs")
    void shouldReturnValidationErrorForNullConfigs() {
        UpdateSystemConfigCommand command = new UpdateSystemConfigCommand(null);

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("empty"));
        verify(systemConfigRepository, never()).save(any());
    }
}
