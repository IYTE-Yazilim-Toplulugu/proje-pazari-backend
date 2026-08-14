package com.iyte_yazilim.proje_pazari.application.queries.getProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetProjectQueryHandlerTest {

    @Mock private ProjectRepository projectRepository;
    @Mock private ProjectMapper projectMapper;
    @Mock private ProjectDetailDtoMapper projectDetailDtoMapper;
    @Mock private MessageService messageService;

    @InjectMocks private GetProjectQueryHandler handler;

    @Test
    @DisplayName("Should return project detail when project exists")
    void shouldReturnProjectDetail_whenProjectExists() {
        String projectId = Ulid.fast().toString();
        ProjectEntity entity = new ProjectEntity();
        entity.setId(projectId);
        Project domain = new Project();
        ProjectDetailDto dto =
                new ProjectDetailDto(
                        projectId,
                        null,
                        "Owner",
                        "owner@test.com",
                        "Title",
                        "Desc",
                        "Summary",
                        0,
                        null,
                        5,
                        null,
                        "Cat",
                        null,
                        null);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(entity));
        when(projectMapper.entityToDomain(entity)).thenReturn(domain);
        when(projectDetailDtoMapper.domainToDto(domain)).thenReturn(dto);
        when(messageService.getMessage("project.retrieved.success")).thenReturn("OK");

        ApiResponse<ProjectDetailDto> response = handler.handle(new GetProjectQuery(projectId));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        assertNotNull(response.getData());
        assertEquals(projectId, response.getData().projectId());
    }

    @Test
    @DisplayName("Should return 404 when project does not exist")
    void shouldReturnNotFound_whenProjectDoesNotExist() {
        String projectId = Ulid.fast().toString();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("project.not.found"), any(Object[].class)))
                .thenReturn("Not found");

        ApiResponse<ProjectDetailDto> response = handler.handle(new GetProjectQuery(projectId));

        assertEquals(ResponseCode.NOT_FOUND, response.getCode());
        assertNull(response.getData());
        verify(projectMapper, never()).entityToDomain(any());
    }
}
