package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import java.util.List;
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
}
