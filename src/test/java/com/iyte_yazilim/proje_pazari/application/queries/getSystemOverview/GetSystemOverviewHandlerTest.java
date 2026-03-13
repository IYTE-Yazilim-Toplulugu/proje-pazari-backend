package com.iyte_yazilim.proje_pazari.application.queries.getSystemOverview;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.iyte_yazilim.proje_pazari.application.dtos.SystemOverviewDTO;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSystemOverviewHandlerTest {

    @Mock private UserRepository userRepository;
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectApplicationRepository applicationRepository;

    @InjectMocks private GetSystemOverviewHandler handler;

    @Test
    @DisplayName("Should return system overview statistics")
    void shouldReturnSystemOverview() {
        when(userRepository.count()).thenReturn(100L);
        when(projectRepository.countByStatus(ProjectStatus.OPEN)).thenReturn(15L);
        when(applicationRepository.countByStatus(ApplicationStatus.PENDING)).thenReturn(25L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(5L);
        when(projectRepository.count()).thenReturn(50L);
        when(applicationRepository.count()).thenReturn(120L);

        for (RoleType role : RoleType.values()) {
            when(userRepository.countByRole(role)).thenReturn(33L);
        }

        ApiResponse<SystemOverviewDTO> response = handler.handle(new GetSystemOverviewQuery());

        assertNotNull(response.getData());
        assertEquals(100L, response.getData().getTotalUsers());
        assertEquals(15L, response.getData().getActiveProjects());
        assertEquals(25L, response.getData().getPendingApplications());
        assertEquals(50L, response.getData().getTotalProjects());
        assertEquals(120L, response.getData().getTotalApplications());
        assertNotNull(response.getData().getUsersByRole());
    }
}
