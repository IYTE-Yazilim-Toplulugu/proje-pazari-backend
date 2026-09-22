package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;

public record UserUpdatedEvent(
        String userId, String email, String firstName, LocalDateTime occurredOn)
        implements IDomainEvent {}
