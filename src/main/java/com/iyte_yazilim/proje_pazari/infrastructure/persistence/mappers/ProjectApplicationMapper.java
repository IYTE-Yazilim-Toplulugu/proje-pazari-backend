package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { UserMapper.class })
public interface ProjectApplicationMapper {

    // Map Domain Entity -> Persistence Entity
    @Mapping(target = "id", expression = "java(projectApplication.getId() != null ? projectApplication.getId().toString() : null)")
    @Mapping(target = "project", ignore = true) // Avoid circular mapping
    ProjectApplicationEntity domainToEntity(ProjectApplication projectApplication);

    // Map Persistence Entity -> Domain Entity
    @Mapping(target = "id", expression = "java(projectApplicationEntity.getId() != null ? com.github.f4b6a3.ulid.Ulid.from(projectApplicationEntity.getId()) : null)")
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "project", ignore = true) // Avoid circular mapping
    ProjectApplication entityToDomain(ProjectApplicationEntity projectApplicationEntity);
}
