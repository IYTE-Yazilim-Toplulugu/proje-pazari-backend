package com.iyte_yazilim.proje_pazari.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;

import com.iyte_yazilim.proje_pazari.domain.events.DomainEvent;

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
public abstract class BaseEntity<TId> {
    TId id;

    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    ArrayList<DomainEvent> domainEvents = new ArrayList<>();

    DomainEvent addomainEvent(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
        return domainEvent;

    }

    DomainEvent removedomainEvent(DomainEvent domainEvent) {
        domainEvents.remove(domainEvent);
        return domainEvent;
    }

    DomainEvent updatedomainEvent(DomainEvent domainEvent) {
        int index = domainEvents.indexOf(domainEvent);
        if (index != -1) {
            domainEvents.set(index, domainEvent);
        }
        return domainEvent;
    }

}
