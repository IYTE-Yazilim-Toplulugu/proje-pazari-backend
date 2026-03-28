package com.iyte_yazilim.proje_pazari.domain.events;

public record UserUpdatedEvent(String userId) implements IDomainEvent {}
