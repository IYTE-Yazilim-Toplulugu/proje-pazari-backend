package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.BannedIpEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for banned IP persistence operations. */
public interface BannedIpRepository extends JpaRepository<BannedIpEntity, String> {

    Optional<BannedIpEntity> findByIpAddress(String ipAddress);

    boolean existsByIpAddress(String ipAddress);

    void deleteByIpAddress(String ipAddress);
}
