package com.iyte_yazilim.proje_pazari.application.common;

/**
 * Marker interface for requests (commands and queries).
 *
 * <p>All commands and queries should implement this interface to be handled by the Mediator.
 *
 * @param <TResponse> the type of response this request produces
 */
public interface IRequest<TResponse> {}
