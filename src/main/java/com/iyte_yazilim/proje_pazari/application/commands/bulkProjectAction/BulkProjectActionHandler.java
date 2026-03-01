package com.iyte_yazilim.proje_pazari.application.commands.bulkProjectAction;

import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.domain.models.ApiResponse;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BulkProjectActionHandler
        implements IRequestHandler<BulkProjectActionCommand, ApiResponse<BulkActionResult>> {

    private final ProjectRepository projectRepository;

    @Override
    @Transactional
    public ApiResponse<BulkActionResult> handle(BulkProjectActionCommand command) {
        BulkActionResult result = new BulkActionResult();

        if (command.projectIds() == null || command.projectIds().isEmpty()) {
            return ApiResponse.validationError("Project IDs list cannot be empty");
        }

        for (String projectId : command.projectIds()) {
            try {
                ProjectEntity project = projectRepository.findById(projectId).orElse(null);
                if (project == null) {
                    result.addFailure(projectId, "Project not found");
                    continue;
                }

                switch (command.action().toUpperCase()) {
                    case "DELETE":
                        projectRepository.delete(project);
                        result.incrementSuccess();
                        break;
                    case "FEATURE":
                        project.setFeatured(true);
                        projectRepository.save(project);
                        result.incrementSuccess();
                        break;
                    case "UNFEATURE":
                        project.setFeatured(false);
                        projectRepository.save(project);
                        result.incrementSuccess();
                        break;
                    case "CANCEL":
                        project.setStatus(ProjectStatus.CANCELLED);
                        projectRepository.save(project);
                        result.incrementSuccess();
                        break;
                    default:
                        result.addFailure(projectId, "Unknown action: " + command.action());
                }
            } catch (Exception e) {
                result.addFailure(projectId, e.getMessage());
            }
        }

        return ApiResponse.success(result, "Bulk project action completed");
    }
}
