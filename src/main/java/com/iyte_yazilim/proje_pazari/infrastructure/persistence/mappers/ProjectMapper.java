package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class, ProjectApplicationMapper.class})
public interface ProjectMapper {

    // Map Domain Entity -> Persistence Entity
    @Mapping(
            target = "id",
            expression = "java(project.getId() != null ? project.getId().toString() : null)")
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "applications", source = "applications")
    ProjectEntity domainToEntity(Project project);

    // Map Persistence Entity -> Domain Entity
    @Mapping(
            target = "id",
            expression =
                    "java(projectEntity.getId() != null ? com.github.f4b6a3.ulid.Ulid.from(projectEntity.getId()) : null)")
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "owner", source = "owner")
    @Mapping(target = "applications", source = "applications")
    Project entityToDomain(ProjectEntity projectEntity);
}
