package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectDocument.OwnerInfo;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.utils.NameUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ProjectDocumentMapper {

    @Mapping(
            target = "status",
            expression = "java(entity.getStatus() != null ? entity.getStatus().name() : null)")
    @Mapping(target = "owner", source = "owner", qualifiedByName = "toOwnerInfo")
    @Mapping(
            target = "applicationsCount",
            expression =
                    "java(entity.getApplications() != null ? entity.getApplications().size() : 0)")
    @Mapping(target = "tags", ignore = true)
    ProjectDocument toDocument(ProjectEntity entity);

    @Named("toOwnerInfo")
    default OwnerInfo toOwnerInfo(UserEntity user) {
        if (user == null) {
            return null;
        }
        OwnerInfo ownerInfo = new OwnerInfo();
        ownerInfo.setId(user.getId());
        ownerInfo.setEmail(user.getEmail());
        ownerInfo.setName(NameUtils.buildFullName(user.getFirstName(), user.getLastName()));
        return ownerInfo;
    }
}
