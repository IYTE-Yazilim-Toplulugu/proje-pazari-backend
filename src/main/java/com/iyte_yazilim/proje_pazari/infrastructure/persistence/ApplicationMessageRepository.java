package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ApplicationMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApplicationMessageRepository
        extends JpaRepository<ApplicationMessageEntity, String> {

    @EntityGraph(attributePaths = "sender")
    Page<ApplicationMessageEntity> findByApplication_Id(String applicationId, Pageable pageable);
}
