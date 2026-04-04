package com.iyte_yazilim.proje_pazari.application.commands.updateProject;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.application.mappers.ProjectDetailDtoMapper;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IRequestHandler;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.ProjectMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.presentation.payload.response.ApiResponse;
import java.util.Arrays;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles project update commands with proper field-level semantics.
 *
 * <p>Update semantics for optional fields:
 *
 * <ul>
 *   <li>{@code null} → field is not changed (existing value preserved)
 *   <li>Empty array {@code []} → clears existing values
 *   <li>Non-empty value → replaces existing value
 * </ul>
 *
 * <p>Projects in {@link ProjectStatus#COMPLETED} or {@link ProjectStatus#CANCELLED} status cannot
 * be updated.
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.1
 * @since 2026-03-23
 */
@Component
@RequiredArgsConstructor
public class UpdateProjectHandler
        implements IRequestHandler<UpdateProjectCommand, ApiResponse<ProjectDetailDto>> {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final ProjectDetailDtoMapper projectDetailDtoMapper;
    private final MessageService messageService;

    @Override
    @Transactional(
            timeoutString = "${spring.transaction.timeout:30}",
            rollbackFor = Exception.class,
            isolation = Isolation.READ_COMMITTED,
            propagation = Propagation.REQUIRED)
    public ApiResponse<ProjectDetailDto> handle(UpdateProjectCommand command) {

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

        // --- 3. Verify Project Status Allows Updates ---
        if (projectEntity.getStatus() == ProjectStatus.COMPLETED
                || projectEntity.getStatus() == ProjectStatus.CANCELLED) {
            return ApiResponse.forbidden(messageService.getMessage("project.update.not.allowed"));
        }

        // --- 4. Apply Updates (null = skip, empty = clear, non-empty = replace) ---
        if (command.projectName() != null) {
            projectEntity.setTitle(command.projectName());
        }
        if (command.description() != null) {
            projectEntity.setDescription(command.description());
        }

        if (command.summary() != null) {
            projectEntity.setSummary(command.summary());
        }
        if (command.maxTeamSize() != null) {
            projectEntity.setMaxTeamSize(command.maxTeamSize());
        }
        if (command.requiredSkills() != null) {
            if (command.requiredSkills().length == 0) {
                projectEntity.setRequiredSkills(Collections.emptyList());
            } else {
                projectEntity.setRequiredSkills(Arrays.asList(command.requiredSkills()));
            }
        }
        if (command.category() != null) {
            projectEntity.setCategory(command.category());
        }
        if (command.deadline() != null) {
            projectEntity.setDeadline(command.deadline());
        }

        // --- 5. Persist ---
        ProjectEntity saved = projectRepository.save(projectEntity);

        // --- 6. Map to DTO ---
        Project domain = projectMapper.entityToDomain(saved);
        ProjectDetailDto dto = projectDetailDtoMapper.domainToDto(domain);

        // --- 7. Response ---
        return ApiResponse.success(dto, messageService.getMessage("project.updated.success"));
    }
}
