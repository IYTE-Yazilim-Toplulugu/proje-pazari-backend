package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingIndexRepository extends JpaRepository<PendingIndexEntity, String> {

    List<PendingIndexEntity> findByStatus(PendingIndexStatus status);

    boolean existsByProjectIdAndStatus(String projectId, PendingIndexStatus status);
}
