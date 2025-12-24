package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;

/**
 * ProjectRepository - JPA Repository for persistence layer
 */
public interface ProjectRepository extends JpaRepository<ProjectEntity, String> {
}
