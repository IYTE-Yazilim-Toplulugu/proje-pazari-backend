package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.AuditLogEntity;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Repository for audit log persistence operations. */
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, String> {

    Page<AuditLogEntity> findByAction(String action, Pageable pageable);

    Page<AuditLogEntity> findByPerformedBy(String performedBy, Pageable pageable);

    Page<AuditLogEntity> findByEntityId(String entityId, Pageable pageable);

    @Query(
            "SELECT a FROM AuditLogEntity a WHERE a.timestamp BETWEEN :start AND :end ORDER BY a.timestamp DESC")
    Page<AuditLogEntity> findByTimestampBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            Pageable pageable);

    @Query(
            "SELECT a FROM AuditLogEntity a WHERE "
                    + "(:action IS NULL OR a.action = :action) AND "
                    + "(:performedBy IS NULL OR a.performedBy = :performedBy) AND "
                    + "(:entityType IS NULL OR a.entityType = :entityType) AND "
                    + "(:entityId IS NULL OR a.entityId = :entityId) "
                    + "ORDER BY a.timestamp DESC")
    Page<AuditLogEntity> findWithFilters(
            @Param("action") String action,
            @Param("performedBy") String performedBy,
            @Param("entityType") String entityType,
            @Param("entityId") String entityId,
            Pageable pageable);
}
