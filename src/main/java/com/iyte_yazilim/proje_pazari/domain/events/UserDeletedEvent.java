package com.iyte_yazilim.proje_pazari.domain.events;

import com.github.f4b6a3.ulid.Ulid;
import lombok.Getter;

@Getter
public class UserDeletedEvent extends DomainEvent {

    private final Ulid userId;

    public UserDeletedEvent(Ulid userId) {
        super("UserDeleted", userId.toString(), "User", java.time.LocalDateTime.now().toString());
        this.userId = userId;
    }
}
