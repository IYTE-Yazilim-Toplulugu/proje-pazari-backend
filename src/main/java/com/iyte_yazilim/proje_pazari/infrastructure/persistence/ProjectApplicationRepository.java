package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ProjectApplicationRepository - JPA Repository for persistence layer */
public interface ProjectApplicationRepository
        extends JpaRepository<ProjectApplicationEntity, String> {

    @Query("SELECT pa FROM ProjectApplicationEntity pa WHERE pa.project.id = :projectId")
    List<ProjectApplicationEntity> findByProjectId(@Param("projectId") String projectId);

    @Query("SELECT pa FROM ProjectApplicationEntity pa WHERE pa.user.id = :userId")
    List<ProjectApplicationEntity> findByUserId(@Param("userId") String userId);

    @Query(
            "SELECT CASE WHEN COUNT(pa) > 0 THEN true ELSE false END "
                    + "FROM ProjectApplicationEntity pa "
                    + "WHERE pa.project.id = :projectId AND pa.user.id = :userId")
    boolean existsByProjectIdAndUserId(
            @Param("projectId") String projectId, @Param("userId") String userId);

    long countByStatus(ApplicationStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query(
            "SELECT pa FROM ProjectApplicationEntity pa WHERE "
                    + "(:status IS NULL OR pa.status = :status) AND "
                    + "(:projectId IS NULL OR pa.project.id = :projectId) AND "
                    + "(:userId IS NULL OR pa.user.id = :userId)")
    Page<ProjectApplicationEntity> findWithFilters(
            @Param("status") ApplicationStatus status,
            @Param("projectId") String projectId,
            @Param("userId") String userId,
            Pageable pageable);
}
