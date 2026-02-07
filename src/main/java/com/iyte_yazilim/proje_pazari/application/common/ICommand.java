package com.iyte_yazilim.proje_pazari.application.common;

/**
 * Marker interface for commands (write operations).
 *
 * <p>Commands represent operations that modify state. They are automatically wrapped in
 * transactions by the TransactionBehavior.
 *
 * @param <TResponse> the type of response this command produces
 */
public interface ICommand<TResponse> extends IRequest<TResponse> {}
