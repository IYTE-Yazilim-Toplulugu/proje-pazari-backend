package com.iyte_yazilim.proje_pazari.application.commands.updateFeatureFlag;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FeatureFlagRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FeatureFlagEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateFeatureFlagHandlerTest {

        @Mock
        private FeatureFlagRepository featureFlagRepository;

        @InjectMocks
        private UpdateFeatureFlagHandler handler;

        @Test
        @DisplayName("Should create a new feature flag when key does not exist")
        void shouldCreateNewFeatureFlag() {
                when(featureFlagRepository.findByFlagKey("new_feature")).thenReturn(Optional.empty());
                when(featureFlagRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

                ApiResponse<Void> response = handler.handle(
                                new UpdateFeatureFlagCommand("new_feature", true, "A new feature"));

                assertNotNull(response);
                assertEquals(ResponseCode.SUCCESS, response.getCode());
                verify(featureFlagRepository).save(any(FeatureFlagEntity.class));
        }

        @Test
        @DisplayName("Should update existing feature flag")
        void shouldUpdateExistingFeatureFlag() {
                FeatureFlagEntity existing = FeatureFlagEntity.builder()
                                .id("flag-1")
                                .flagKey("existing_feature")
                                .enabled(false)
                                .description("Old description")
                                .build();
                when(featureFlagRepository.findByFlagKey("existing_feature"))
                                .thenReturn(Optional.of(existing));
                when(featureFlagRepository.save(any())).thenReturn(existing);

                ApiResponse<Void> response = handler.handle(
                                new UpdateFeatureFlagCommand("existing_feature", true, "Updated description"));

                assertNotNull(response);
                assertTrue(existing.isEnabled());
                assertEquals("Updated description", existing.getDescription());
                verify(featureFlagRepository).save(existing);
        }

        @Test
        @DisplayName("Should return validation error for blank key")
        void shouldReturnValidationErrorForBlankKey() {
                ApiResponse<Void> response = handler.handle(
                                new UpdateFeatureFlagCommand("", true, "desc"));

                assertNotNull(response);
                assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
                verify(featureFlagRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should return validation error for null key")
        void shouldReturnValidationErrorForNullKey() {
                ApiResponse<Void> response = handler.handle(
                                new UpdateFeatureFlagCommand(null, true, "desc"));

                assertNotNull(response);
                assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
                verify(featureFlagRepository, never()).save(any());
        }
}
