package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import java.util.Optional;

/** Domain repository interface for user persistence operations. */
public interface IUserRepository {

    Optional<User> findByEmail(String email);

    Optional<User> findById(String id);

    boolean existsByEmail(String email);

    User save(User entity);
}
