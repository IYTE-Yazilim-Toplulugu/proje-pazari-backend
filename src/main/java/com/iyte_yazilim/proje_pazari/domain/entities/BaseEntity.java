package com.iyte_yazilim.proje_pazari.domain.entities;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.iyte_yazilim.proje_pazari.domain.events.DomainEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

/**
 * Base entity class that provides common fields for all entities.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity<TId> {

    @Id
    protected TId id;
    
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt;

    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
    
    @Transient 
    protected List<DomainEvent> domainEvents = new ArrayList<>();

    @PrePersist 
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate 
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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

    public void clearDomainEvents() {
        domainEvents.clear();
    }
}