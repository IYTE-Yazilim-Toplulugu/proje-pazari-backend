package com.iyte_yazilim.proje_pazari.domain.events;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DomainEvent {
    private String eventType;
    private String entityId;
    private String entityType;
    private String timestamp;

    public DomainEvent(String eventType, String entityId, String entityType, String timestamp) {
        this.eventType = eventType;
        this.entityId = entityId;
        this.entityType = entityType;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DomainEvent{" +
                "eventType='" + eventType + '\'' +
                ", entityId='" + entityId + '\'' +
                ", entityType='" + entityType + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof DomainEvent))
            return false;
        DomainEvent that = (DomainEvent) o;
        return eventType.equals(that.eventType) &&

                entityId.equals(that

                        .entityId)
                &&
                entityType.equals(that.entityType) &&
                timestamp.equals(that.timestamp);
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
