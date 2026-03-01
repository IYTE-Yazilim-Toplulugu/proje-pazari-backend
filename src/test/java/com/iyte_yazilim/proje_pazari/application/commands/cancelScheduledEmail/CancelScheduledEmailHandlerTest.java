package com.iyte_yazilim.proje_pazari.application.commands.cancelScheduledEmail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CancelScheduledEmailHandlerTest {

    @Mock private ScheduledEmailRepository scheduledEmailRepository;

    @InjectMocks private CancelScheduledEmailHandler handler;

    @Test
    @DisplayName("Should cancel pending scheduled email")
    void shouldCancelPendingEmail() {
        ScheduledEmailEntity entity =
                ScheduledEmailEntity.builder()
                        .id("email-1")
                        .subject("Test")
                        .body("Body")
                        .status("PENDING")
                        .build();
        when(scheduledEmailRepository.findById("email-1")).thenReturn(Optional.of(entity));
        when(scheduledEmailRepository.save(any())).thenReturn(entity);

        ApiResponse<Void> response = handler.handle(new CancelScheduledEmailCommand("email-1"));

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals("CANCELLED", entity.getStatus());
        verify(scheduledEmailRepository).save(entity);
    }

    @Test
    @DisplayName("Should return not found for unknown email ID")
    void shouldReturnNotFoundForUnknownEmailId() {
        when(scheduledEmailRepository.findById("unknown")).thenReturn(Optional.empty());

        ApiResponse<Void> response = handler.handle(new CancelScheduledEmailCommand("unknown"));

        assertNotNull(response);
        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        verify(scheduledEmailRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error when cancelling non-pending email")
    void shouldReturnValidationErrorForNonPendingEmail() {
        ScheduledEmailEntity entity =
                ScheduledEmailEntity.builder()
                        .id("email-2")
                        .subject("Sent Email")
                        .body("Body")
                        .status("SENT")
                        .build();
        when(scheduledEmailRepository.findById("email-2")).thenReturn(Optional.of(entity));

        ApiResponse<Void> response = handler.handle(new CancelScheduledEmailCommand("email-2"));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        verify(scheduledEmailRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for blank email ID")
    void shouldReturnValidationErrorForBlankEmailId() {
        ApiResponse<Void> response = handler.handle(new CancelScheduledEmailCommand(""));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return validation error for null email ID")
    void shouldReturnValidationErrorForNullEmailId() {
        ApiResponse<Void> response = handler.handle(new CancelScheduledEmailCommand(null));

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }
}
