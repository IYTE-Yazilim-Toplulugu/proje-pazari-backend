package com.iyte_yazilim.proje_pazari.application.mappers;

import com.iyte_yazilim.proje_pazari.application.dtos.ProjectDetailDto;
import com.iyte_yazilim.proje_pazari.domain.entities.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectDetailDtoMapper {

    @Mapping(
            target = "ownerName",
            expression =
                    "java(project.getOwner() != null ? project.getOwner().getFirstName() + \" \" + project.getOwner().getLastName() : null)")
    @Mapping(
            target = "ownerEmail",
            expression = "java(project.getOwner() != null ? project.getOwner().getEmail() : null)")
    @Mapping(
            target = "applicationCount",
            expression =
                    "java(project.getApplications() != null ? project.getApplications().size() : 0)")
    ProjectDetailDto domainToDto(Project project);
}
