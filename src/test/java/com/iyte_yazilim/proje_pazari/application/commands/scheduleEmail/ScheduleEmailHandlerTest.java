package com.iyte_yazilim.proje_pazari.application.commands.scheduleEmail;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ScheduledEmailRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ScheduledEmailEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ResponseCode;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduleEmailHandlerTest {

    @Mock private ScheduledEmailRepository scheduledEmailRepository;

    @InjectMocks private ScheduleEmailHandler handler;

    @Test
    @DisplayName("Should schedule email successfully")
    void shouldScheduleEmailSuccessfully() {
        LocalDateTime futureTime = LocalDateTime.now().plusDays(1);
        when(scheduledEmailRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ScheduleEmailCommand command =
                new ScheduleEmailCommand("Test Subject", "Test Body", "ALL", futureTime);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(scheduledEmailRepository).save(any(ScheduledEmailEntity.class));
    }

    @Test
    @DisplayName("Should return validation error for missing subject")
    void shouldReturnValidationErrorForMissingSubject() {
        ScheduleEmailCommand command =
                new ScheduleEmailCommand(null, "body", "ALL", LocalDateTime.now().plusDays(1));
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
        verify(scheduledEmailRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return validation error for blank subject")
    void shouldReturnValidationErrorForBlankSubject() {
        ScheduleEmailCommand command =
                new ScheduleEmailCommand("  ", "body", "ALL", LocalDateTime.now().plusDays(1));
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return validation error for missing body")
    void shouldReturnValidationErrorForMissingBody() {
        ScheduleEmailCommand command =
                new ScheduleEmailCommand("Subject", null, "ALL", LocalDateTime.now().plusDays(1));
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return validation error for null scheduled time")
    void shouldReturnValidationErrorForNullScheduledTime() {
        ScheduleEmailCommand command = new ScheduleEmailCommand("Subject", "Body", "ALL", null);
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }

    @Test
    @DisplayName("Should return validation error for past scheduled time")
    void shouldReturnValidationErrorForPastTime() {
        ScheduleEmailCommand command =
                new ScheduleEmailCommand(
                        "Subject", "Body", "ALL", LocalDateTime.now().minusDays(1));
        ApiResponse<Void> response = handler.handle(command);

        assertNotNull(response);
        assertEquals(ResponseCode.VALIDATION_ERROR, response.getCode());
    }
}
