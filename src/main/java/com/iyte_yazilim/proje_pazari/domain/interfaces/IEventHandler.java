package com.iyte_yazilim.proje_pazari.domain.interfaces;

/**
 * Interface for handling domain events.
 *
 * <p>Event handlers process domain events in a loosely coupled manner, following the event-driven
 * architecture pattern.
 *
 * @param <TEvent> the type of domain event to handle
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2026-01-16
 */
public interface IEventHandler<TEvent> {
    /**
     * Handles a domain event.
     *
     * @param event the domain event to handle
     */
    void handle(TEvent event);
}
