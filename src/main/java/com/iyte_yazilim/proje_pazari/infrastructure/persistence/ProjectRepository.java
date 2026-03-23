package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ProjectRepository - JPA Repository for persistence layer */
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    @Query("SELECT p FROM ProjectEntity p WHERE p.owner.id = :ownerId")
    List<ProjectEntity> findByOwnerId(@Param("ownerId") String ownerId);

    long countByStatus(ProjectStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);




    @Query(
            "SELECT p FROM ProjectEntity p WHERE "
                    + "(:status IS NULL OR p.status = :status) AND "
                    + "(:ownerId IS NULL OR p.owner.id = :ownerId) AND "
                    + "(:search IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) "
                    + "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<ProjectEntity> findWithFilters(
            @Param("status") ProjectStatus status,
            @Param("ownerId") String ownerId,
            @Param("search") String search,
            Pageable pageable);
}
