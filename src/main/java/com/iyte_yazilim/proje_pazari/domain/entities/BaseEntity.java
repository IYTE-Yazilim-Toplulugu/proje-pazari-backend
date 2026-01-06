package com.iyte_yazilim.proje_pazari.domain.entities;

import com.iyte_yazilim.proje_pazari.domain.events.DomainEvent;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity class that provides common properties and methods for all entities.
 *
 * @param <TId> The type of the entity identifier.
 */
@Getter
@Setter
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
