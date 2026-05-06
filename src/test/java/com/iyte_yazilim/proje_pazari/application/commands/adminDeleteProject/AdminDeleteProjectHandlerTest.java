package com.iyte_yazilim.proje_pazari.application.commands.adminDeleteProject;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ProjectNotFoundException;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDeleteProjectHandlerTest {

    @Mock private ProjectRepository projectRepository;

    @InjectMocks private AdminDeleteProjectHandler handler;

    private String projectId;
    private ProjectEntity projectEntity;

    @BeforeEach
    void setUp() {
        projectId = Ulid.fast().toString();
        projectEntity = new ProjectEntity();
        projectEntity.setId(projectId);
        projectEntity.setTitle("Test Project");
    }

    @Test
    @DisplayName("Should delete project successfully")
    void shouldDeleteProject_whenProjectExists() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(projectEntity));

        ApiResponse<Void> response = handler.handle(new AdminDeleteProjectCommand(projectId));

        assertEquals(ResponseCode.SUCCESS, response.getCode());
        verify(projectRepository).delete(projectEntity);
    }

    @Test
    @DisplayName("Should throw ProjectNotFoundException when project does not exist")
    void shouldThrowException_whenProjectNotFound() {
        String unknownId = Ulid.fast().toString();
        when(projectRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThrows(
                ProjectNotFoundException.class,
                () -> handler.handle(new AdminDeleteProjectCommand(unknownId)));
        verify(projectRepository, never()).delete(any());
    }
}
