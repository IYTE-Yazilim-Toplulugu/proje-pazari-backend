package com.iyte_yazilim.proje_pazari.application.queries.getAllProjects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
class GetAllProjectsQueryHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectDetailDtoMapper projectDtoMapper;
    @Mock private MessageService messageService;

    @InjectMocks private GetAllProjectsQueryHandler handler;

    @BeforeEach
    void setUp() {
        lenient().when(messageService.getMessage("projects.listed.success")).thenReturn("Listed");
    }

    @Test
    @DisplayName("Should return paged projects with correct pagination metadata")
    void shouldReturnPagedProjects_withPaginationMetadata() {
        ProjectEntity entity = new ProjectEntity();
        Project domain = new Project();
        ProjectDetailDto dto =
                new ProjectDetailDto(
                        "id1", null, null, "Title", null, null, 0, null, null, null, null, null,
                        null);

        Page<ProjectEntity> page = new PageImpl<>(List.of(entity));
        when(projectRepository.findAllWithApplications(any(Pageable.class))).thenReturn(page);
        when(projectMapper.entityToDomain(entity)).thenReturn(domain);
        when(projectDtoMapper.domainToDto(domain)).thenReturn(dto);

        ApiResponse<PagedProjectsResult> response =
                handler.handle(new GetAllProjectsQuery(0, 10, "title", "ASC"));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertEquals(1, response.getData().projects().size());
        assertEquals(0, response.getData().currentPage());
        assertEquals(1, response.getData().totalPages());
        assertEquals(1L, response.getData().totalElements());
    }

    @Test
    @DisplayName("Should default to 'id' when sortBy field is not in allowlist")
    void shouldDefaultSortById_whenSortByIsInvalid() {
        Page<ProjectEntity> emptyPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findAllWithApplications(pageableCaptor.capture()))
                .thenReturn(emptyPage);

        handler.handle(new GetAllProjectsQuery(0, 10, "invalidField", "ASC"));

        Pageable captured = pageableCaptor.getValue();
        Sort.Order order = captured.getSort().getOrderFor("id");
        assertNotNull(order, "Sort should fall back to 'id'");
    }

    @Test
    @DisplayName("Should sort descending when sortDirection is DESC")
    void shouldSortDescending_whenSortDirectionIsDesc() {
        Page<ProjectEntity> emptyPage = new PageImpl<>(List.of());
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        when(projectRepository.findAllWithApplications(pageableCaptor.capture()))
                .thenReturn(emptyPage);

        handler.handle(new GetAllProjectsQuery(0, 10, "title", "DESC"));

        Pageable captured = pageableCaptor.getValue();
        Sort.Order order = captured.getSort().getOrderFor("title");
        assertNotNull(order);
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }
}
