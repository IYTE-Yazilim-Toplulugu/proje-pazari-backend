package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Map Domain Entity -> Persistence Entity
    @Mapping(
            target = "id",
            expression = "java(user.getId() != null ? user.getId().toString() : null)")
    // FIX: Map domain 'active' to entity 'isActive'
    @Mapping(target = "isActive", source = "active")
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "preferredLanguage", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    UserEntity domainToEntity(User user);

    // Map Persistence Entity -> Domain Entity
    @Mapping(
            target = "id",
            expression =
                    "java(userEntity.getId() != null ? com.github.f4b6a3.ulid.Ulid.from(userEntity.getId()) : null)")
    @Mapping(target = "domainEvents", ignore = true)
    // FIX: Map entity 'isActive' to domain 'active'
    @Mapping(target = "active", source = "isActive")
    // FIX: Ignore roles if the Domain User doesn't have a matching roles collection yet
    @Mapping(target = "roles", ignore = true)
    User entityToDomain(UserEntity userEntity);
}
