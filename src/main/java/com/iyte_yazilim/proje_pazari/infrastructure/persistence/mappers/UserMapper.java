package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.github.f4b6a3.ulid.Ulid;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Map Domain Entity -> Persistence Entity
    @Mapping(target = "id", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    UserEntity domainToEntity(User user);

    // Map Persistence Entity -> Domain Entity
    @Mapping(target = "id", expression = "java(userEntity.getId() != null ? com.github.f4b6a3.ulid.Ulid.from(userEntity.getId()) : null)")
    @Mapping(target = "domainEvents", ignore = true)
    User entityToDomain(UserEntity userEntity);
}
