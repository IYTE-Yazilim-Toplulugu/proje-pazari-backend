package com.iyte_yazilim.proje_pazari.application.common;

/**
 * Handler that processes a request and returns a response.
 *
 * @param <TRequest> request type
 * @param <TResponse> response type
 */
public interface IRequestHandler<TRequest extends IRequest<TResponse>, TResponse> {
    TResponse handle(TRequest request);
}
