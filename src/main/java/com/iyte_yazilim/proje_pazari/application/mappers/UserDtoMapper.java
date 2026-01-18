package com.iyte_yazilim.proje_pazari.application.mappers;

import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    // Map Domain Entity -> DTO
    @Mapping(
            target = "userId",
            expression = "java(user.getId() != null ? user.getId().toString() : null)")
    @Mapping(target = "preferredLanguage", ignore = true)
    UserDto domainToDto(User user);

    // Map UserEntity -> DTO
    @Mapping(target = "userId", source = "id")
    UserDto toDto(UserEntity userEntity);
}
