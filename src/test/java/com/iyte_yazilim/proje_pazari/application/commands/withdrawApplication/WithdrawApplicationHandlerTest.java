package com.iyte_yazilim.proje_pazari.application.commands.withdrawApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectApplicationMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WithdrawApplicationHandlerTest {

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private ProjectApplicationMapper applicationMapper;
    @Mock private MessageService messageService;

    @InjectMocks private WithdrawApplicationHandler handler;

    private String applicationId;
    private String userId;
    private UserEntity applicantUser;
    private ProjectApplicationEntity application;

    @BeforeEach
    void setUp() {
        applicationId = Ulid.fast().toString();
        userId = Ulid.fast().toString();

        applicantUser = new UserEntity();
        applicantUser.setId(userId);

        application = new ProjectApplicationEntity();
        application.setId(applicationId);
        application.setUser(applicantUser);
        application.setStatus(ApplicationStatus.PENDING);
    }

    @Test
    @DisplayName("Should withdraw a PENDING application successfully")
    void shouldWithdrawApplication_whenPendingAndOwner() {
        ProjectApplication domainApp = new ProjectApplication();

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationMapper.entityToDomain(application)).thenReturn(domainApp);
        when(messageService.getMessage("application.withdrawn.success")).thenReturn("Withdrawn");

        ApiResponse<Void> response =
                handler.handle(new WithdrawApplicationCommand(applicationId, userId));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
        verify(applicationRepository).save(application);
    }

    @Test
    @DisplayName("Should return 404 when application does not exist")
    void shouldReturnNotFound_whenApplicationDoesNotExist() {
        String unknownId = Ulid.fast().toString();
        when(applicationRepository.findById(unknownId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("application.not.found"), any(Object[].class)))
                .thenReturn("Not found");

        ApiResponse<Void> response =
                handler.handle(new WithdrawApplicationCommand(unknownId, userId));

        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 403 when applicant ID does not match the requester")
    void shouldReturnForbidden_whenApplicantIdMismatch() {
        String otherUserId = Ulid.fast().toString();
        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(messageService.getMessage("application.withdraw.forbidden")).thenReturn("Forbidden");

        ApiResponse<Void> response =
                handler.handle(new WithdrawApplicationCommand(applicationId, otherUserId));

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 400 when application status is not PENDING")
    void shouldReturnBadRequest_whenApplicationIsNotPending() {
        application.setStatus(ApplicationStatus.APPROVED);

        ProjectApplication domainApp = new ProjectApplication();
        domainApp.reconstitute(null, null, ApplicationStatus.APPROVED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationMapper.entityToDomain(application)).thenReturn(domainApp);
        when(messageService.getMessage("application.withdraw.not.pending"))
                .thenReturn("Not pending");

        ApiResponse<Void> response =
                handler.handle(new WithdrawApplicationCommand(applicationId, userId));

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 400 when application is WITHDRAWN")
    void shouldReturnBadRequest_whenApplicationIsAlreadyWithdrawn() {
        application.setStatus(ApplicationStatus.WITHDRAWN);

        ProjectApplication domainApp = new ProjectApplication();
        domainApp.reconstitute(null, null, ApplicationStatus.WITHDRAWN);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationMapper.entityToDomain(application)).thenReturn(domainApp);
        when(messageService.getMessage("application.withdraw.not.pending"))
                .thenReturn("Not pending");

        ApiResponse<Void> response =
                handler.handle(new WithdrawApplicationCommand(applicationId, userId));

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        verify(applicationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should return 400 when application is REJECTED")
    void shouldReturnBadRequest_whenApplicationIsRejected() {
        application.setStatus(ApplicationStatus.REJECTED);

        ProjectApplication domainApp = new ProjectApplication();
        domainApp.reconstitute(null, null, ApplicationStatus.REJECTED);

        when(applicationRepository.findById(applicationId)).thenReturn(Optional.of(application));
        when(applicationMapper.entityToDomain(application)).thenReturn(domainApp);
        when(messageService.getMessage("application.withdraw.not.pending"))
                .thenReturn("Not pending");

        ApiResponse<Void> response =
                handler.handle(new WithdrawApplicationCommand(applicationId, userId));

        assertEquals(ResponseCode.BAD_REQUEST, response.getCode());
        verify(applicationRepository, never()).save(any());
    }
}
