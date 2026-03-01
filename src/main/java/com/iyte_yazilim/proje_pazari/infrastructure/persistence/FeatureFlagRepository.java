package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FeatureFlagEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/** Repository for feature flag persistence operations. */
public interface FeatureFlagRepository extends JpaRepository<FeatureFlagEntity, String> {

    Optional<FeatureFlagEntity> findByFlagKey(String flagKey);

    boolean existsByFlagKey(String flagKey);
}
