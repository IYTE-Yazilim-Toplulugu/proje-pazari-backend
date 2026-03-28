package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.SystemConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for system configuration persistence operations. */
public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, String> {

    Optional<SystemConfigEntity> findByConfigKey(String configKey);
}
