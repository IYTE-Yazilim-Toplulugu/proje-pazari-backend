package com.iyte_yazilim.proje_pazari.domain.entities;

import com.iyte_yazilim.proje_pazari.domain.events.DomainEvent;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity class that provides common fields for all entities.
 *
 * <p>Responsibilities: - Provides auto-generated primary key (id) - Tracks creation timestamp
 * (createdAt) - Tracks last modification timestamp (updatedAt)
 *
 * <p>All entities should extend this class to inherit these common fields.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity<TId> {

    protected TId id;

    protected LocalDateTime createdAt;

    protected LocalDateTime updatedAt;

    protected List<DomainEvent> domainEvents = new ArrayList<>();

    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    protected DomainEvent addDomainEvent(DomainEvent domainEvent) {
        domainEvents.add(domainEvent);
        return domainEvent;
    }

    protected DomainEvent removeDomainEvent(DomainEvent domainEvent) {
        domainEvents.remove(domainEvent);
        return domainEvent;
    }

    protected DomainEvent updateDomainEvent(DomainEvent domainEvent) {
        int index = domainEvents.indexOf(domainEvent);
        if (index != -1) {
            domainEvents.set(index, domainEvent);
        }
        return domainEvent;
    }

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
