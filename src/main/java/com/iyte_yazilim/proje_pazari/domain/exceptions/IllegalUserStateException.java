package com.iyte_yazilim.proje_pazari.domain.exceptions;

/**
 * Thrown when a {@link com.iyte_yazilim.proje_pazari.domain.entities.User} lifecycle method is
 * invoked from an invalid current state.
 *
 * <p>For example, calling {@code deactivate()} on a user who is already inactive.
 *
 * @author IYTE Yazılım Topluluğu
 * @since 2026-04-04
 */
public class IllegalUserStateException extends IllegalStateException {

    public IllegalUserStateException(String message) {
        super(message);
    }
}
