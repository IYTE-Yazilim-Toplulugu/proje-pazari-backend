package com.iyte_yazilim.proje_pazari.application.mappers;

import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterUserMapper {

    // Map Command -> Domain Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "linkedinUrl", ignore = true)
    @Mapping(target = "githubUrl", ignore = true)
    User commandToDomain(RegisterUserCommand command);

    // Map Domain Entity -> Result DTO
    @Mapping(target = "userId", expression = "java(user.getId() != null ? user.getId().toString() : null)")
    RegisterUserResult domainToResult(User user);
}
