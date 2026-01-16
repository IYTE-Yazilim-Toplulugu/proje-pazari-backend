package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;

public interface IDomainEvent {
    /** Event occurrence timestamp */
    default LocalDateTime occurredOn() {
        return LocalDateTime.now();
    }

    /** Event type identifier */
    default String eventType() {
        return this.getClass().getSimpleName();
    }
}
