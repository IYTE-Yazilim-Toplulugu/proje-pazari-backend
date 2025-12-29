package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;

public record UserRegisteredEvent(
        String userId,
        String email,
        String firstName,
        String verificationToken,
        LocalDateTime occurredOn
) implements IDomainEvent {}
