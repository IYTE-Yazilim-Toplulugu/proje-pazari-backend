package com.example.demo.domain.entities;

import java.time.LocalDateTime;
import com.example.demo.domain.events.DomainEvent;

/**
 * Base entity class that provides common fields for all entities.
 * 
 * Responsibilities:
 * - Provides auto-generated primary key (id)
 * - Tracks creation timestamp (createdAt)
 * - Tracks last modification timestamp (updatedAt)
 * 
 * All entities should extend this class to inherit these common fields.
 */
public abstract class BaseEntity {
    Long id;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    abstract DomainEvent adddomainEvent();

    abstract DomainEvent removedomainEvent();

    abstract DomainEvent updatedomainEvent();

}
