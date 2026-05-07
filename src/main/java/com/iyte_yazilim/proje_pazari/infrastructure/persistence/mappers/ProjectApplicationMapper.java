package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.domain.entities.ProjectApplication;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(
        componentModel = "spring",
        uses = {UserMapper.class})
public interface ProjectApplicationMapper {

    // Map Domain Entity -> Persistence Entity
    @Mapping(
            target = "id",
            expression =
                    "java(projectApplication.getId() != null ? projectApplication.getId().toString() : null)")
    @Mapping(target = "project", ignore = true) // Avoid circular mapping
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProjectApplicationEntity domainToEntity(ProjectApplication projectApplication);

    /**
     * Internal base mapping — populates scalar fields only. Marked as {@code @Named("base")} so
     * MapStruct does not select it as an automatic type-conversion candidate (avoids ambiguity with
     * the public {@link #entityToDomain(ProjectApplicationEntity)} method).
     */
    @Named("base")
    @Mapping(
            target = "id",
            expression =
                    "java(projectApplicationEntity.getId() != null ? com.github.f4b6a3.ulid.Ulid.from(projectApplicationEntity.getId()) : null)")
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "project", ignore = true) // Avoid circular mapping
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "status", ignore = true)
    ProjectApplication entityToDomainBase(ProjectApplicationEntity projectApplicationEntity);

    /**
     * Maps a persistence entity to the domain entity, reconstituting state through the
     * infrastructure-only method to avoid triggering domain guards.
     */
    default ProjectApplication entityToDomain(ProjectApplicationEntity entity) {
        if (entity == null) {
            return null;
        }
        ProjectApplication domain = entityToDomainBase(entity);
        // Use reconstitute to bypass domain guards — this is infrastructure mapping
        domain.reconstitute(null, null, entity.getStatus());
        return domain;
    }
}
