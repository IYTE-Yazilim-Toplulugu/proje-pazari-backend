package com.iyte_yazilim.proje_pazari.domain.interfaces;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;

/**
 * Handler interface for processing requests.
 *
 * @param <TRequest> the type of request this handler processes
 * @param <TResponse> the type of response this handler produces
 */
public interface IRequestHandler<TRequest extends IRequest<TResponse>, TResponse> {
    /**
     * Handles a request and returns a response.
     *
     * @param request the request to handle
     * @return the response after handling the request
     */
    TResponse handle(TRequest request);
}
