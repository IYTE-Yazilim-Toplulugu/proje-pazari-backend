package com.iyte_yazilim.proje_pazari.application.mappers;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.application.commands.createProject.CreateProjectCommand;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.models.results.CreateProjectCommandResult;
import java.util.Arrays;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CreateProjectMapper {

    @Mapping(target = "title", source = "projectName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "owner", expression = "java(createUserWithId(command.ownerId()))")
    @Mapping(target = "summary", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "currentTeamSize", constant = "1") // Owner başlangıçta takımın parçası
    @Mapping(
            target = "requiredSkills",
            expression = "java(convertArrayToList(command.requiredSkills()))")
    Project commandToDomain(CreateProjectCommand command);

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
    @Mapping(
            target = "requiredSkills",
            expression = "java(convertListToArray(project.getRequiredSkills()))")
    CreateProjectCommandResult domainToResult(Project project);

    default User createUserWithId(String ownerId) {
        if (ownerId == null || ownerId.isBlank()) {
            return null;
        }
        User user = new User();
        user.setId(Ulid.from(ownerId));
        return user;
    }

    default List<String> convertArrayToList(String[] array) {
        if (array == null) {
            return null;
        }
        return Arrays.asList(array);
    }

    default String[] convertListToArray(List<String> list) {
        if (list == null) {
            return new String[0];
        }
        return list.toArray(new String[0]);
    }
}
