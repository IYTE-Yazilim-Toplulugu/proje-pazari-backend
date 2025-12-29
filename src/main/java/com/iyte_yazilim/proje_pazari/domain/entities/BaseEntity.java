package com.iyte_yazilim.proje_pazari.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.iyte_yazilim.proje_pazari.domain.events.DomainEvent;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

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

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity<TId> {
    protected TId id;

    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    @Transient
    protected ArrayList<DomainEvent> domainEvents = new ArrayList<>();

    protected DomainEvent addomainEvent(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
        return domainEvent;

    }

    protected DomainEvent removedomainEvent(DomainEvent domainEvent) {
        domainEvents.remove(domainEvent);
        return domainEvent;
    }

    protected DomainEvent updatedomainEvent(DomainEvent domainEvent) {
        int index = domainEvents.indexOf(domainEvent);
        if (index != -1) {
            domainEvents.set(index, domainEvent);
        }
        return domainEvent;
    }

}
