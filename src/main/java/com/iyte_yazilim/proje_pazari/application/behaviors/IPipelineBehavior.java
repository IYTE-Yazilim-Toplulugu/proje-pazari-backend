package com.iyte_yazilim.proje_pazari.application.behaviors;

import com.iyte_yazilim.proje_pazari.application.common.IRequest;
import com.iyte_yazilim.proje_pazari.application.common.RequestHandlerDelegate;

/**
 * Pipeline behavior interface for cross-cutting concerns.
 *
 * <p>Behaviors are executed in order around the request handler, allowing for pre and post
 * processing of requests.
 *
 * @param <TRequest> the type of request
 * @param <TResponse> the type of response
 */
public interface IPipelineBehavior<TRequest extends IRequest<TResponse>, TResponse> {
    /**
     * Handles the request, potentially delegating to the next behavior or handler.
     *
     * @param request the request being handled
     * @param next the next handler in the pipeline
     * @return the response
     */
    TResponse handle(TRequest request, RequestHandlerDelegate<TResponse> next);
}
