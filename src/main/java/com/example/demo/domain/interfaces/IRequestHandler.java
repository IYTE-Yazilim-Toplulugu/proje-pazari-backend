package com.example.demo.domain.interfaces;

public interface IRequestHandler<TRequest, TResponse> {
    /**
     * Handles a request and returns a response.
     *
     * @param request the request to handle
     * @return the response after handling the request
     */
    TResponse handle(TRequest request);
}
