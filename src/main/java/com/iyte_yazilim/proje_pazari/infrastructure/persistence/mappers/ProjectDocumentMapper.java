package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectDocumentMapper {

    @Mapping(
            target = "status",
            expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "ownerId", source = "owner.id")
    @Mapping(
            target = "ownerName",
            expression =
                    "java(entity.getOwner() != null ? com.iyte_yazilim.proje_pazari.infrastructure.utils.NameUtils.buildFullName(entity.getOwner().getFirstName(), entity.getOwner().getLastName()) : null)")
    @Mapping(
            target = "ownerEmail",
            expression = "java(entity.getOwner() != null ? entity.getOwner().getEmail() : null)")
    @Mapping(
            target = "applicationCount",
            expression =
                    "java(entity.getApplications() != null ? entity.getApplications().size() : 0)")
    ProjectDocument toDocument(ProjectEntity entity);
}
