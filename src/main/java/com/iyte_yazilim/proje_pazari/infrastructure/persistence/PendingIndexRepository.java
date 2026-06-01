package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.PendingIndexEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PendingIndexRepository extends JpaRepository<PendingIndexEntity, String> {

    List<PendingIndexEntity> findByStatus(String status);
}
