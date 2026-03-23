package com.iyte_yazilim.proje_pazari.application.commands.deleteProject;

import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DeleteProjectHandler
        implements IRequestHandler<DeleteProjectCommand, ApiResponse<Void>> {

    private final ProjectRepository projectRepository;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<Void> handle(DeleteProjectCommand command) {

        // --- 1. Verify Project Exists ---
        ProjectEntity projectEntity = projectRepository.findById(command.projectId()).orElse(null);
        if (projectEntity == null) {
            return ApiResponse.notFound(
                    messageService.getMessage(
                            "project.not.found", new Object[] {command.projectId()}));
        }

        // --- 2. Verify Ownership ---
        if (!projectEntity.getOwner().getId().equals(command.ownerId())) {
            return ApiResponse.forbidden(messageService.getMessage("project.owner.mismatch"));
        }

        // --- 3. Delete ---
        projectRepository.delete(projectEntity);

        // --- 4. Response ---
        return ApiResponse.success(null, messageService.getMessage("project.deleted.success"));
    }
}
