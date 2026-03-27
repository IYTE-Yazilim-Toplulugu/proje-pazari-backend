package com.iyte_yazilim.proje_pazari.application.queries.getUserProjects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.dtos.PagedProjectsResult;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class GetUserProjectsHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectDetailDtoMapper projectDetailDtoMapper;
    @Mock private MessageService messageService;

    @InjectMocks private GetUserProjectsHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(messageService.getMessage("projects.listed.success")).thenReturn("Listed");
    }

    @Test
    @DisplayName("Should return projects owned by the given user")
    void shouldReturnUserProjects() {
        String userId = Ulid.fast().toString();
        ProjectEntity entity = new ProjectEntity();
        Project domain = new Project();
        ProjectDetailDto dto =
                new ProjectDetailDto(
                        "id1", null, null, "Title", null, null, 0, null, null, null, null, null,
                        null);

        Page<ProjectEntity> page = new PageImpl<>(List.of(entity));
        when(projectRepository.findWithFilters(isNull(), eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(page);
        when(projectMapper.entityToDomain(entity)).thenReturn(domain);
        when(projectDetailDtoMapper.domainToDto(domain)).thenReturn(dto);

        ApiResponse<PagedProjectsResult> response =
                handler.handle(new GetUserProjectsQuery(userId, 0, 10, "createdAt", "DESC"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().projects().size());
    }

    @Test
    @DisplayName("Should return empty result when user has no projects")
    void shouldReturnEmptyResult_whenUserHasNoProjects() {
        String userId = Ulid.fast().toString();
        Page<ProjectEntity> emptyPage = new PageImpl<>(List.of());
        when(projectRepository.findWithFilters(isNull(), eq(userId), isNull(), any(Pageable.class)))
                .thenReturn(emptyPage);

        ApiResponse<PagedProjectsResult> response =
                handler.handle(new GetUserProjectsQuery(userId, 0, 10, "createdAt", "DESC"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertTrue(response.getData().projects().isEmpty());
        assertEquals(0L, response.getData().totalElements());
    }

    @Test
    @DisplayName("Should default to 'id' when sortBy field is not in allowlist")
    void shouldDefaultSortById_whenSortByIsInvalid() {
        String userId = Ulid.fast().toString();
        Page<ProjectEntity> emptyPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findWithFilters(
                        isNull(), eq(userId), isNull(), pageableCaptor.capture()))
                .thenReturn(emptyPage);

        handler.handle(new GetUserProjectsQuery(userId, 0, 10, "injectedField; DROP TABLE", "ASC"));

        Pageable captured = pageableCaptor.getValue();
        Sort.Order order = captured.getSort().getOrderFor("id");
        assertNotNull(order, "Sort should fall back to 'id' for unrecognized field");
    }
}
