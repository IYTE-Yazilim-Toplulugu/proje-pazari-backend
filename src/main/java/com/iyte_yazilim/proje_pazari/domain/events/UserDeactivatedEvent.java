package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;
import lombok.Getter;

/** Published after a user account is deactivated or admin-deleted. */
@Getter
public class UserDeactivatedEvent extends DomainEvent {

    private final String userId;
    private final String email;

    public UserDeactivatedEvent(String userId, String email) {
        super("UserDeactivated", userId, "User", LocalDateTime.now().toString());
        this.userId = userId;
        this.email = email;
    }
}
