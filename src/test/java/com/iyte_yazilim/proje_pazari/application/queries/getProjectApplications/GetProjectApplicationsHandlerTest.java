package com.iyte_yazilim.proje_pazari.application.queries.getProjectApplications;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.ApplicationDto;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
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
class GetProjectApplicationsHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private MessageService messageService;

    @InjectMocks private GetProjectApplicationsHandler handler;

    private String projectId;
    private String ownerId;
    private ProjectEntity projectEntity;
    private UserEntity ownerEntity;

    @BeforeEach
    void setUp() {
        projectId = Ulid.fast().toString();
        ownerId = Ulid.fast().toString();

        ownerEntity = new UserEntity();
        ownerEntity.setId(ownerId);
        ownerEntity.setFirstName("Alice");
        ownerEntity.setLastName("Smith");
        ownerEntity.setEmail("alice@test.com");

        projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setTitle("Test Project");
        projectEntity.setOwner(ownerEntity);
    }

    @Test
    @DisplayName("Should return all applications when owner requests without status filter")
    void shouldReturnAllApplications_whenOwnerRequestsWithoutFilter() {
        UserEntity applicant = buildUser();
        ProjectApplicationEntity app = buildApp(applicant, ApplicationStatus.PENDING);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(applicationRepository.findByProjectIdWithOptionalStatus(projectId, null))
                .thenReturn(List.of(app));
        when(messageService.getMessage("application.list.retrieved.success")).thenReturn("OK");

        ApiResponse<List<ApplicationDto>> response =
                handler.handle(new GetProjectApplicationsQuery(projectId, ownerId, null));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(app.getId(), response.getData().get(0).applicationId());
        assertEquals(projectId, response.getData().get(0).projectId());
    }

    @Test
    @DisplayName("Should filter applications by status when status filter is provided")
    void shouldFilterApplicationsByStatus_whenStatusFilterProvided() {
        UserEntity applicant = buildUser();
        ProjectApplicationEntity pending = buildApp(applicant, ApplicationStatus.PENDING);
        ProjectApplicationEntity approved = buildApp(applicant, ApplicationStatus.APPROVED);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(applicationRepository.findByProjectIdWithOptionalStatus(
                        projectId, ApplicationStatus.PENDING))
                .thenReturn(List.of(pending));
        when(messageService.getMessage("application.list.retrieved.success")).thenReturn("OK");

        ApiResponse<List<ApplicationDto>> response =
                handler.handle(
                        new GetProjectApplicationsQuery(
                                projectId, ownerId, ApplicationStatus.PENDING));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals(ApplicationStatus.PENDING, response.getData().get(0).status());
    }

    @Test
    @DisplayName("Should return 404 when project does not exist")
    void shouldReturnNotFound_whenProjectDoesNotExist() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("project.not.found"), any(Object[].class)))
                .thenReturn("Not found");

        ApiResponse<List<ApplicationDto>> response =
                handler.handle(new GetProjectApplicationsQuery(projectId, ownerId, null));

        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        verify(applicationRepository, never()).findByProjectId(any());
    }

    @Test
    @DisplayName("Should return 403 when requester is not the project owner")
    void shouldReturnForbidden_whenRequesterIsNotOwner() {
        String nonOwnerId = Ulid.fast().toString();
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(messageService.getMessage("project.owner.mismatch")).thenReturn("Forbidden");

        ApiResponse<List<ApplicationDto>> response =
                handler.handle(new GetProjectApplicationsQuery(projectId, nonOwnerId, null));

        assertEquals(ResponseCode.FORBIDDEN, response.getCode());
        verify(applicationRepository, never()).findByProjectId(any());
    }

    @Test
    @DisplayName("Should map entity fields to ApplicationDto correctly")
    void shouldMapEntityToDto_correctly() {
        UserEntity applicant = buildUser();
        ProjectApplicationEntity app = buildApp(applicant, ApplicationStatus.APPROVED);
        app.setReviewMessage("Welcome aboard");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));
        when(applicationRepository.findByProjectIdWithOptionalStatus(projectId, null))
                .thenReturn(List.of(app));
        when(messageService.getMessage("application.list.retrieved.success")).thenReturn("OK");

        ApiResponse<List<ApplicationDto>> response =
                handler.handle(new GetProjectApplicationsQuery(projectId, ownerId, null));

        ApplicationDto dto = response.getData().get(0);
        assertEquals(app.getId(), dto.applicationId());
        assertEquals(projectId, dto.projectId());
        assertEquals("Test Project", dto.projectTitle());
        assertEquals(applicant.getId(), dto.applicantId());
        assertEquals("Bob Jones", dto.applicantName());
        assertEquals(ApplicationStatus.APPROVED, dto.status());
        assertEquals("Welcome aboard", dto.reviewMessage());
    }

    private UserEntity buildUser() {
        UserEntity user = new UserEntity();
        user.setId(Ulid.fast().toString());
        user.setFirstName("Bob");
        user.setLastName("Jones");
        user.setEmail("bob@test.com");
        return user;
    }

    private ProjectApplicationEntity buildApp(UserEntity user, ApplicationStatus status) {
        ProjectApplicationEntity app = new ProjectApplicationEntity();
        app.setId(Ulid.fast().toString());
        app.setProject(projectEntity);
        app.setUser(user);
        app.setStatus(status);
        return app;
    }
}
