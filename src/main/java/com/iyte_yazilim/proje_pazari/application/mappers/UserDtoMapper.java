package com.iyte_yazilim.proje_pazari.application.mappers;

import com.iyte_yazilim.proje_pazari.application.dtos.UserDto;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    // Map Domain Entity -> DTO
    @Mapping(target = "userId", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    UserDto domainToDto(User user);
}
