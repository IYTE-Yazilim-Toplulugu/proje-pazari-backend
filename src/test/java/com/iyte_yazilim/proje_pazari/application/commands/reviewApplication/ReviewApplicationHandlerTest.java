package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReviewApplicationHandlerTest {

    @Mock private ProjectApplicationRepository applicationRepository;

    // --- ADDED MOCKS FOR DOMAIN REFACTORING ---
    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    // ------------------------------------------

    @Mock private MessageService messageService;
    @Mock private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks private ReviewApplicationHandler handler;

    private String applicationId;
    private ProjectApplicationEntity applicationEntity;

    @BeforeEach
    void setUp() {
        applicationId = Ulid.fast().toString();

        UserEntity owner = new UserEntity();
        owner.setId(Ulid.fast().toString());
        owner.setFirstName("Owner");
        owner.setEmail("owner@std.iyte.edu.tr");

        ProjectEntity project = new ProjectEntity();
        project.setId(Ulid.fast().toString());
        project.setTitle("Test Project");
        project.setOwner(owner);

        UserEntity applicant = new UserEntity();
        applicant.setId(Ulid.fast().toString());
        applicant.setFirstName("Applicant");
        applicant.setEmail("applicant@std.iyte.edu.tr");

        applicationEntity = new ProjectApplicationEntity();
        applicationEntity.setId(applicationId);
        applicationEntity.setStatus(ApplicationStatus.PENDING);
        applicationEntity.setProject(project);
        applicationEntity.setUser(applicant);

        lenient()
                .when(messageService.getMessage("application.reviewed.success"))
                .thenReturn("Application reviewed successfully");
    }

    @Test
    @DisplayName("Should approve application successfully")
    void shouldApproveApplication_whenApplicationExists() {
        ReviewApplicationCommand command =
                new ReviewApplicationCommand(applicationId, ApplicationStatus.APPROVED, "Good fit");

        // --- ADDED DOMAIN MOCK BEHAVIOR ---
        Project mockDomainProject = new Project();
        // Project must be OPEN and have room (current size 0, max 5) to pass validation
        mockDomainProject.reconstitute(ProjectStatus.OPEN, 0);
        mockDomainProject.setMaxTeamSize(5);
        when(projectMapper.entityToDomain(any())).thenReturn(mockDomainProject);
        // ----------------------------------

        when(applicationRepository.findById(applicationId))
                .thenReturn(Optional.of(applicationEntity));
        when(projectRepository.findByIdWithLock(applicationEntity.getProject().getId()))
                .thenReturn(Optional.of(applicationEntity.getProject()));
        when(applicationRepository.save(applicationEntity)).thenReturn(applicationEntity);

        ApiResponse<ReviewApplicationCommandResult> response = handler.handle(command);

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(ApplicationStatus.APPROVED, applicationEntity.getStatus());
        verify(applicationEventPublisher).publishEvent(any(Object.class));

        // Verify the team size was persisted
        verify(projectRepository).save(any(ProjectEntity.class));
    }

    @Test
    @DisplayName("Should throw ApplicationNotFoundException when application does not exist")
    void shouldThrowException_whenApplicationNotFound() {
        String unknownId = Ulid.fast().toString();
        ReviewApplicationCommand command =
                new ReviewApplicationCommand(unknownId, ApplicationStatus.APPROVED, null);

        when(applicationRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(ApplicationNotFoundException.class, () -> handler.handle(command));
        verify(applicationRepository, never()).save(any());
        verify(applicationEventPublisher, never()).publishEvent(any(Object.class));
    }
}
