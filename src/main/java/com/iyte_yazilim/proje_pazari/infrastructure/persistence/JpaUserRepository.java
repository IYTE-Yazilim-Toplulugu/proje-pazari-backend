package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.domain.entities.User;
import com.iyte_yazilim.proje_pazari.domain.interfaces.IUserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.mappers.UserMapper;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Adapter that bridges the Spring Data {@link UserRepository} to the domain interface. */
@Component
@RequiredArgsConstructor
public class JpaUserRepository implements IUserRepository {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(userMapper::entityToDomain);
    }

    @Override
    public Optional<User> findById(String id) {
        return userRepository.findById(id).map(userMapper::entityToDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public User save(User domain) {
        UserEntity entity;
        if (domain.getId() != null) {
            // Update: load existing entity to preserve JPA-managed fields,
            // then apply domain-level changes.
            entity =
                    userRepository
                            .findById(domain.getId().toString())
                            .orElseGet(() -> userMapper.domainToEntity(domain));
            userMapper.applyDomainToEntity(domain, entity);
        } else {
            entity = userMapper.domainToEntity(domain);
        }
        return userMapper.entityToDomain(userRepository.save(entity));
    }
}
