package com.iyte_yazilim.proje_pazari.application.queries.getAnalyticsTrends;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.AnalyticsTrendsDTO;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAnalyticsTrendsHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectApplicationRepository applicationRepository;

    @InjectMocks private GetAnalyticsTrendsHandler handler;

    @Test
    void shouldReturnTrendsForSpecifiedDays() {
        when(userRepository.countByCreatedAtBetween(
                        any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(5L);
        when(projectRepository.countByCreatedAtBetween(
                        any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(3L);
        when(applicationRepository.countByCreatedAtBetween(
                        any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(10L);

        ApiResponse<AnalyticsTrendsDTO> response = handler.handle(new GetAnalyticsTrendsQuery(7));

        assertNotNull(response);
        assertNotNull(response.getData());
        assertEquals(7, response.getData().totalDays());
        assertEquals(7, response.getData().dataPoints().size());

        // Verify each data point has correct values
        for (AnalyticsTrendsDTO.DailyDataPoint dp : response.getData().dataPoints()) {
            assertEquals(5L, dp.newUsers());
            assertEquals(3L, dp.newProjects());
            assertEquals(10L, dp.newApplications());
            assertNotNull(dp.date());
        }

        // Verify dates are in ascending order
        for (int i = 1; i < response.getData().dataPoints().size(); i++) {
            assertTrue(
                    response.getData()
                            .dataPoints()
                            .get(i)
                            .date()
                            .isAfter(response.getData().dataPoints().get(i - 1).date()));
        }
    }

    @Test
    void shouldClampDaysToMinimumOne() {
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(projectRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(applicationRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);

        ApiResponse<AnalyticsTrendsDTO> response = handler.handle(new GetAnalyticsTrendsQuery(0));

        assertNotNull(response);
        assertEquals(1, response.getData().totalDays());
        assertEquals(1, response.getData().dataPoints().size());
    }

    @Test
    void shouldClampDaysToMaximum365() {
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(projectRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);
        when(applicationRepository.countByCreatedAtBetween(any(), any())).thenReturn(0L);

        ApiResponse<AnalyticsTrendsDTO> response = handler.handle(new GetAnalyticsTrendsQuery(500));

        assertNotNull(response);
        assertEquals(365, response.getData().totalDays());
        assertEquals(365, response.getData().dataPoints().size());
    }

    @Test
    void shouldReturnTodayInDataPoints() {
        when(userRepository.countByCreatedAtBetween(any(), any())).thenReturn(1L);
        when(projectRepository.countByCreatedAtBetween(any(), any())).thenReturn(2L);
        when(applicationRepository.countByCreatedAtBetween(any(), any())).thenReturn(3L);

        ApiResponse<AnalyticsTrendsDTO> response = handler.handle(new GetAnalyticsTrendsQuery(1));

        assertNotNull(response);
        assertEquals(1, response.getData().dataPoints().size());
        assertEquals(LocalDate.now(), response.getData().dataPoints().get(0).date());
        assertEquals(1L, response.getData().dataPoints().get(0).newUsers());
        assertEquals(2L, response.getData().dataPoints().get(0).newProjects());
        assertEquals(3L, response.getData().dataPoints().get(0).newApplications());
    }
}
