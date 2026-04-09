package com.iyte_yazilim.proje_pazari.domain.events;

import lombok.Getter;

@Getter
public class UserDeletedEvent extends DomainEvent {

    private final String userId;

    public UserDeletedEvent(String userId) {
        super("UserDeleted", userId, "User", java.time.LocalDateTime.now().toString());
        this.userId = userId;
    }
}
