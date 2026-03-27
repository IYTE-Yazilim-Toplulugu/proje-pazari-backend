package com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers;

import com.iyte_yazilim.proje_pazari.domain.entities.PasswordResetToken;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PasswordResetTokenEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PasswordResetTokenMapper {

    PasswordResetTokenEntity domainToEntity(PasswordResetToken domain);

    PasswordResetToken entityToDomain(PasswordResetTokenEntity entity);
}
