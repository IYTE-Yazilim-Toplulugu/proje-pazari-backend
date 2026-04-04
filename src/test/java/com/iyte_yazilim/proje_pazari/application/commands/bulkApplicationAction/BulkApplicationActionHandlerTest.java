package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkApplicationActionHandlerTest {

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private ProjectApplicationMapper applicationMapper;

    @InjectMocks private BulkApplicationActionHandler handler;

    private String appId;
    private ProjectApplicationEntity applicationEntity;

    @BeforeEach
    void setUp() {
        appId = Ulid.fast().toString();
        applicationEntity = new ProjectApplicationEntity();
        applicationEntity.setId(appId);
        applicationEntity.setStatus(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("Should approve applications in bulk")
    void shouldApproveApplicationsInBulk() {
        ProjectApplication domainApp = new ProjectApplication();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(applicationEntity));
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);
        when(applicationRepository.save(any())).thenReturn(applicationEntity);

        ApiResponse<BulkActionResult> response =
                handler.handle(new BulkApplicationActionCommand("APPROVE", List.of(appId)));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(0, response.getData().getFailureCount());
        assertEquals(ApplicationStatus.APPROVED, applicationEntity.getStatus());
    }

    @Test
    @DisplayName("Should reject applications in bulk")
    void shouldRejectApplicationsInBulk() {
        ProjectApplication domainApp = new ProjectApplication();

        when(applicationRepository.findById(appId)).thenReturn(Optional.of(applicationEntity));
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);
        when(applicationRepository.save(any())).thenReturn(applicationEntity);

        ApiResponse<BulkActionResult> response =
                handler.handle(new BulkApplicationActionCommand("REJECT", List.of(appId)));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(ApplicationStatus.REJECTED, applicationEntity.getStatus());
    }

    @Test
    @DisplayName("Should record failure when application is not found")
    void shouldRecordFailure_whenApplicationNotFound() {
        String unknownId = Ulid.fast().toString();
        when(applicationRepository.findById(unknownId)).thenReturn(Optional.empty());

        ApiResponse<BulkActionResult> response =
                handler.handle(new BulkApplicationActionCommand("APPROVE", List.of(unknownId)));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(0, response.getData().getSuccessCount());
        assertEquals(1, response.getData().getFailureCount());
    }

    @Test
    @DisplayName("Should process partial successes in bulk")
    void shouldProcessPartialSuccess_whenSomeApplicationsNotFound() {
        String foundId = Ulid.fast().toString();
        String missingId = Ulid.fast().toString();

        ProjectApplicationEntity foundEntity = new ProjectApplicationEntity();
        foundEntity.setId(foundId);
        foundEntity.setStatus(ApplicationStatus.PENDING);

        ProjectApplication domainApp = new ProjectApplication();

        when(applicationRepository.findById(foundId)).thenReturn(Optional.of(foundEntity));
        when(applicationMapper.entityToDomain(foundEntity)).thenReturn(domainApp);
        when(applicationRepository.findById(missingId)).thenReturn(Optional.empty());
        when(applicationRepository.save(any())).thenReturn(foundEntity);

        ApiResponse<BulkActionResult> response =
                handler.handle(
                        new BulkApplicationActionCommand("APPROVE", List.of(foundId, missingId)));

        assertEquals(1, response.getData().getSuccessCount());
        assertEquals(1, response.getData().getFailureCount());
    }

    @Test
    @DisplayName("Should return validation error for empty list")
    void shouldReturnValidationError_whenApplicationIdsIsEmpty() {
        ApiResponse<BulkActionResult> response =
                handler.handle(new BulkApplicationActionCommand("APPROVE", List.of()));

        assertNotNull(response);
        verify(applicationRepository, never()).findById(any());
    }
}
