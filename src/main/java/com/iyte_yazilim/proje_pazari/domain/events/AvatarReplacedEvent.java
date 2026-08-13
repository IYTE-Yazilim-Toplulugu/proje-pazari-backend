package com.iyte_yazilim.proje_pazari.domain.events;

import java.time.LocalDateTime;

/**
 * Published after a replacement avatar object has been stored and its database reference has
 * committed. Consumed after commit to delete the previous avatar object, so a replacement that
 * fails to commit never loses the object the (unchanged) database value still points at.
 */
public record AvatarReplacedEvent(
        String userId, String previousStoredValue, String newStoredValue, LocalDateTime occurredOn)
        implements IDomainEvent {}
