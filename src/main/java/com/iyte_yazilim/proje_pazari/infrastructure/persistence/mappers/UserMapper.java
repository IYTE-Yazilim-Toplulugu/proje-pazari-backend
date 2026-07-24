package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Map Domain Entity -> Persistence Entity
    @Mapping(
            target = "id",
            expression = "java(user.getId() != null ? user.getId().toString() : null)")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity domainToEntity(User user);

    // Map Persistence Entity -> Domain Entity
    @Mapping(
            target = "id",
            expression =
                    "java(userEntity.getId() != null ? com.github.f4b6a3.ulid.Ulid.from(userEntity.getId()) : null)")
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User entityToDomain(UserEntity userEntity);

    @AfterMapping
    default void hydrateUserFromEntity(UserEntity source, @MappingTarget User target) {
        if (source.getIsActive() != null) {
            target.reconstituteActive(source.getIsActive());
        }
        target.reconstituteRoles(source.getRoles());
    }

    // Update existing Persistence Entity from Domain Entity (for updates)
    @Mapping(
            target = "id",
            expression = "java(user.getId() != null ? user.getId().toString() : null)")
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "roles", source = "roles")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void applyDomainToEntity(User user, @MappingTarget UserEntity entity);
}
