package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.FlaggedContentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for flagged content persistence operations. */
public interface FlaggedContentRepository extends JpaRepository<FlaggedContentEntity, String> {

    Page<FlaggedContentEntity> findByStatus(String status, Pageable pageable);

    @Query(
            "SELECT f FROM FlaggedContentEntity f WHERE "
                    + "(:status IS NULL OR f.status = :status) AND "
                    + "(:contentType IS NULL OR f.contentType = :contentType) "
                    + "ORDER BY f.createdAt DESC")
    Page<FlaggedContentEntity> findWithFilters(
            @Param("status") String status,
            @Param("contentType") String contentType,
            Pageable pageable);
}
