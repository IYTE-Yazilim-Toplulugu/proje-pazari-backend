package com.iyte_yazilim.proje_pazari.application.common;

/**
 * Mediator interface for sending requests to their handlers.
 *
 * <p>The mediator decouples controllers from handlers by providing a single entry point for all
 * request handling. It automatically discovers and invokes the appropriate handler for each
 * request.
 */
public interface IMediator {
    /**
     * Sends a request to its handler and returns the response.
     *
     * @param <TResponse> the type of response expected
     * @param request the request to handle
     * @return the response from the handler
     * @throws IllegalStateException if no handler is found for the request
     */
    <TResponse> TResponse send(IRequest<TResponse> request);
}
