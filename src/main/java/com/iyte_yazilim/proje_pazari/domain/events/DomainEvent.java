package com.iyte_yazilim.proje_pazari.domain.events;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for domain events in the event-driven architecture.
 *
 * <p>Domain events represent something significant that happened in the domain. They are used to
 * communicate changes between different parts of the system in a loosely coupled manner.
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * DomainEvent event = new DomainEvent(
 *         "USER_REGISTERED",
 *         "01HQXYZ123",
 *         "User",
 *         "2024-01-15T10:30:00Z");
 * }</pre>
 *
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see com.iyte_yazilim.proje_pazari.domain.entities.BaseEntity#addDomainEvent(DomainEvent)
 */
@Getter
@Setter
public class DomainEvent {

    /** Type of event that occurred (e.g., "USER_REGISTERED", "PROJECT_CREATED"). */
    private String eventType;

    /** Unique identifier of the entity that triggered this event. */
    private String entityId;

    /** Type of entity that triggered this event (e.g., "User", "Project"). */
    private String entityType;

    /** ISO 8601 timestamp when the event occurred. */
    private String timestamp;

    /**
     * Creates a new domain event.
     *
     * @param eventType type of event that occurred
     * @param entityId unique identifier of the triggering entity
     * @param entityType type of the triggering entity
     * @param timestamp ISO 8601 timestamp of when the event occurred
     */
    public DomainEvent(String eventType, String entityId, String entityType, String timestamp) {
        this.eventType = eventType;
        this.entityId = entityId;
        this.entityType = entityType;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DomainEvent{"
                + "eventType='"
                + eventType
                + '\''
                + ", entityId='"
                + entityId
                + '\''
                + ", entityType='"
                + entityType
                + '\''
                + ", timestamp='"
                + timestamp
                + '\''
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DomainEvent)) return false;
        DomainEvent that = (DomainEvent) o;
        return eventType.equals(that.eventType)
                && entityId.equals(that.entityId)
                && entityType.equals(that.entityType)
                && timestamp.equals(that.timestamp);
    }

    @Override
    public int hashCode() {
        int result = eventType.hashCode();
        result = 31 * result + entityId.hashCode();
        result = 31 * result + entityType.hashCode();
        result = 31 * result + timestamp.hashCode();
        return result;
    }
}
