package com.iyte_yazilim.proje_pazari.application.mappers;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreateProjectMapper {

    // Map Command -> Domain Entity
    @Mapping(target = "title", source = "projectName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "owner", expression = "java(createUserWithId(command.ownerId()))")
    @Mapping(target = "summary", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "applications", ignore = true)
    Project commandToDomain(CreateProjectCommand command);

    // Map Domain Entity -> Result DTO
    @Mapping(
            target = "projectId",
            expression = "java(project.getId() != null ? project.getId().toString() : null)")
    @Mapping(target = "projectName", source = "title")
    @Mapping(
            target = "ownerId",
            expression =
                    "java(project.getOwner() != null ? project.getOwner().getId().toString() : null)")
    @Mapping(target = "teamMemberIds", expression = "java(new String[0])")
    @Mapping(target = "tags", expression = "java(new String[0])")
    CreateProjectCommandResult domainToResult(Project project);

    // Helper method to create User with just ID
    default User createUserWithId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            return null;
        }
        User user = new User();
        user.setId(Ulid.from(ownerId));
        return user;
    }
}
