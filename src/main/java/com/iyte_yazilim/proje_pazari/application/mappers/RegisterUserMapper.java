package com.iyte_yazilim.proje_pazari.application.mappers;

import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.models.results.RegisterUserResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for user registration operations.
 *
 * <p>Handles transformations between:
 *
 * <ul>
 *   <li>{@link RegisterUserCommand} → {@link User} (command to domain)
 *   <li>{@link User} → {@link RegisterUserResult} (domain to result)
 * </ul>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see RegisterUserCommand
 * @see RegisterUserResult
 */
@Mapper(componentModel = "spring")
public interface RegisterUserMapper {

    /**
     * Maps registration command to domain entity.
     *
     * <p>Ignores fields that are auto-generated or optional at registration.
     *
     * @param command the registration command
     * @return new User domain entity (without ID, timestamps, or optional fields)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "domainEvents", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "profilePictureUrl", ignore = true)
    @Mapping(target = "linkedinUrl", ignore = true)
    @Mapping(target = "githubUrl", ignore = true)
    User commandToDomain(RegisterUserCommand command);

    /**
     * Maps domain entity to registration result.
     *
     * @param user the saved user entity
     * @return registration result with user ID and basic info
     */
    @Mapping(
            target = "userId",
            expression = "java(user.getId() != null ? user.getId().toString() : null)")
    RegisterUserResult domainToResult(User user);
}
