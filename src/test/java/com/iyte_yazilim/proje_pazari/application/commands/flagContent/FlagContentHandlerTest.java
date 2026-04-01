package com.iyte_yazilim.proje_pazari.application.commands.flagContent;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.FlaggedContentRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FlaggedContentEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FlagContentHandlerTest {

    @Mock private FlaggedContentRepository flaggedContentRepository;

    @InjectMocks private FlagContentHandler handler;

    @Test
    @DisplayName("Should flag content successfully")
    void shouldFlagContentSuccessfully() {
        FlagContentCommand command = new FlagContentCommand("PROJECT", "project-123", "SPAM");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals("Content flagged successfully", response.getMessage());

        ArgumentCaptor<FlaggedContentEntity> captor =
                ArgumentCaptor.forClass(FlaggedContentEntity.class);
        verify(flaggedContentRepository).save(captor.capture());
        assertEquals("PROJECT", captor.getValue().getContentType());
        assertEquals("project-123", captor.getValue().getContentId());
        assertEquals("SPAM", captor.getValue().getReason());
    }

    @Test
    @DisplayName("Should return validation error for null content type")
    void shouldReturnValidationErrorForNullContentType() {
        FlagContentCommand command = new FlagContentCommand(null, "id-1", "SPAM");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("required"));
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for invalid content type")
    void shouldReturnValidationErrorForInvalidContentType() {
        FlagContentCommand command = new FlagContentCommand("INVALID", "id-1", "SPAM");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("Invalid content type"));
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for invalid reason")
    void shouldReturnValidationErrorForInvalidReason() {
        FlagContentCommand command = new FlagContentCommand("USER", "id-1", "INVALID_REASON");

        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertTrue(response.getMessage().contains("Invalid reason"));
        verify(flaggedContentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should default reason to INAPPROPRIATE when null")
    void shouldDefaultReasonToInappropriate() {
        FlagContentCommand command = new FlagContentCommand("USER", "id-1", null);

        handler.handle(command);

        ArgumentCaptor<FlaggedContentEntity> captor =
                ArgumentCaptor.forClass(FlaggedContentEntity.class);
        verify(flaggedContentRepository).save(captor.capture());
        assertEquals("INAPPROPRIATE", captor.getValue().getReason());
    }
}
