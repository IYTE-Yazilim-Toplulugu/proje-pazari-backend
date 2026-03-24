package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;

/** Domain repository interface for user persistence operations. */
public interface IUserRepository {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findById(String id);

    boolean existsByEmail(String email);

    UserEntity save(UserEntity entity);
}
