package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.domain.entities.Project;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;

/**
 * ProjectRepository
 */
public interface ProjectRepository extends JpaRepository<Project, Ulid> {
    Project findById(Ulid id);
}
