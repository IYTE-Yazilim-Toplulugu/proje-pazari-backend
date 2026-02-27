package com.iyte_yazilim.proje_pazari.application.common;

/**
 * Functional interface representing the next handler in the pipeline.
 *
 * @param <TResponse> the type of response
 */
@FunctionalInterface
public interface RequestHandlerDelegate<TResponse> {
    TResponse handle();
}
