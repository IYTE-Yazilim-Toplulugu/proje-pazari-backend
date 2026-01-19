package com.iyte_yazilim.proje_pazari.domain.interfaces;

/**
 * Marker interface for CQRS requests (commands and queries).
 *
 * <p>All commands and queries in the system should implement this interface. The type parameter
 * specifies the expected response type for the request.
 *
 * <h2>Example Usage:</h2>
 *
 * <pre>{@code
 * public record RegisterUserCommand(
 *         String email,
 *         String password) implements IRequest<ApiResponse<RegisterUserResult>> {
 * }
 * }</pre>
 *
 * @param <TResponse> the type of response expected from handling this request
 * @author IYTE Yazılım Topluluğu
 * @version 1.0
 * @since 2024-01-01
 * @see IRequestHandler
 */
public interface IRequest<TResponse> {}
