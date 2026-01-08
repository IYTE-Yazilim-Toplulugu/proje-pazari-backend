package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** ProjectRepository - JPA Repository for persistence layer */
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {

    @Query("SELECT p FROM ProjectEntity p WHERE p.owner.id = :ownerId")
    List<ProjectEntity> findByOwnerId(@Param("ownerId") String ownerId);
}
