package com.iyte_yazilim.proje_pazari.application.commands.adminReviewApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalApplicationStateException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminReviewApplicationHandlerTest {

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private ProjectApplicationMapper applicationMapper;

    @InjectMocks private AdminReviewApplicationHandler handler;

    private String applicationId;
    private ProjectApplicationEntity applicationEntity;

    @BeforeEach
    void setUp() {
        applicationId = Ulid.fast().toString();
        applicationEntity = new ProjectApplicationEntity();
        applicationEntity.setId(applicationId);
        applicationEntity.setStatus(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("Should approve application successfully")
    void shouldApproveApplication_whenApplicationExists() {
        ProjectApplication domainApp = new ProjectApplication();

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);

        ApiResponse<Void> response =
                handler.handle(
                        new AdminReviewApplicationCommand(
                                applicationId, ApplicationStatus.APPROVED));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.APPROVED, applicationEntity.getStatus());
        verify(applicationRepository).save(applicationEntity);
    }

    @Test
    @DisplayName("Should reject application successfully")
    void shouldRejectApplication_whenApplicationExists() {
        ProjectApplication domainApp = new ProjectApplication();

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);

        ApiResponse<Void> response =
                handler.handle(
                        new AdminReviewApplicationCommand(
                                applicationId, ApplicationStatus.REJECTED));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.REJECTED, applicationEntity.getStatus());
        verify(applicationRepository).save(applicationEntity);
    }

    @Test
    @DisplayName("Should throw when approving non-PENDING application")
    void shouldThrow_whenApprovingNonPendingApplication() {
        applicationEntity.setStatus(ApplicationStatus.APPROVED);
        ProjectApplication domainApp = new ProjectApplication();
        domainApp.reconstitute(null, null, ApplicationStatus.APPROVED);

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(applicationMapper.entityToDomain(applicationEntity)).thenReturn(domainApp);

        assertThrows(
                IllegalApplicationStateException.class,
                () ->
                        handler.handle(
                                new AdminReviewApplicationCommand(
                                        applicationId, ApplicationStatus.APPROVED)));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ApplicationNotFoundException when application does not exist")
    void shouldThrowException_whenApplicationNotFound() {
        String unknownId = Ulid.fast().toString();
        when(applicationRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(
                ApplicationNotFoundException.class,
                () ->
                        handler.handle(
                                new AdminReviewApplicationCommand(
                                        unknownId, ApplicationStatus.APPROVED)));
        verify(applicationRepository, never()).save(any());
    }
}
