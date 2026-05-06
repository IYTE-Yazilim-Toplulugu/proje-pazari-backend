package com.iyte_yazilim.proje_pazari.application.queries.getUserApplications;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedApplicationsResult;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class GetUserApplicationsHandlerTest {

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private MessageService messageService;

    @InjectMocks private GetUserApplicationsHandler handler;

    private UserEntity user;
    private ProjectEntity project;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(Ulid.fast().toString());
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane@test.com");

        project = new ProjectEntity();
        project.setId(Ulid.fast().toString());
        project.setTitle("Test Project");

        lenient()
                .when(messageService.getMessage("application.list.retrieved.success"))
                .thenReturn("OK");
    }

    @Test
    @DisplayName("Should return paged applications for a user without status filter")
    void shouldReturnPagedApplications_withoutStatusFilter() {
        ProjectApplicationEntity app = buildApp(ApplicationStatus.PENDING);
        Page<ProjectApplicationEntity> page = new PageImpl<>(List.of(app));
        when(applicationRepository.findWithFilters(
                        isNull(), isNull(), eq(user.getId()), any(Pageable.class)))
                .thenReturn(page);

        ApiResponse<PagedApplicationsResult> response =
                handler.handle(new GetUserApplicationsQuery(user.getId(), 0, 10, null));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().applications().size());
        assertEquals(1L, response.getData().totalElements());
    }

    @Test
    @DisplayName("Should filter applications by status when status is provided")
    void shouldPassStatusFilter_toRepository() {
        Page<ProjectApplicationEntity> emptyPage = new PageImpl<>(List.of());
        when(applicationRepository.findWithFilters(
                        eq(ApplicationStatus.APPROVED),
                        isNull(),
                        eq(user.getId()),
                        any(Pageable.class)))
                .thenReturn(emptyPage);

        ApiResponse<PagedApplicationsResult> response =
                handler.handle(
                        new GetUserApplicationsQuery(
                                user.getId(), 0, 10, ApplicationStatus.APPROVED));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(applicationRepository)
                .findWithFilters(
                        eq(ApplicationStatus.APPROVED),
                        isNull(),
                        eq(user.getId()),
                        any(Pageable.class));
    }

    @Test
    @DisplayName("Should map application entity fields to ApplicationDto correctly")
    void shouldMapEntityToDto_correctly() {
        ProjectApplicationEntity app = buildApp(ApplicationStatus.PENDING);
        Page<ProjectApplicationEntity> page = new PageImpl<>(List.of(app));
        when(applicationRepository.findWithFilters(
                        isNull(), isNull(), eq(user.getId()), any(Pageable.class)))
                .thenReturn(page);

        ApiResponse<PagedApplicationsResult> response =
                handler.handle(new GetUserApplicationsQuery(user.getId(), 0, 10, null));

        var dto = response.getData().applications().get(0);
        assertEquals(app.getId(), dto.applicationId());
        assertEquals(project.getId(), dto.projectId());
        assertEquals(project.getTitle(), dto.projectTitle());
        assertEquals(user.getId(), dto.applicantId());
        assertEquals("Jane Doe", dto.applicantName());
        assertEquals(ApplicationStatus.PENDING, dto.status());
    }

    private ProjectApplicationEntity buildApp(ApplicationStatus status) {
        ProjectApplicationEntity app = new ProjectApplicationEntity();
        app.setId(Ulid.fast().toString());
        app.setProject(project);
        app.setUser(user);
        app.setStatus(status);
        return app;
    }
}
