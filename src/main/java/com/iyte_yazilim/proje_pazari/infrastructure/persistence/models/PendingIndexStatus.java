package com.iyte_yazilim.proje_pazari.infrastructure.persistence.models;

/** Lifecycle state of a {@link PendingIndexEntity} retry entry. */
public enum PendingIndexStatus {
    /** Awaiting (re)indexing; picked up by the scheduled retry loop. */
    PENDING,
    /** Successfully indexed; terminal. */
    DONE,
    /** Permanently failed (project gone or max attempts exceeded); terminal, no longer retried. */
    FAILED
}
