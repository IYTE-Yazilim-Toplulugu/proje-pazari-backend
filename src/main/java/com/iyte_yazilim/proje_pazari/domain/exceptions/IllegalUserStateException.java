package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

/**
 * Thrown when a {@link com.iyte_yazilim.proje_pazari.domain.entities.User} lifecycle method is
 * invoked from an invalid current state.
 *
 * <p>For example, calling {@code deactivate()} on a user who is already inactive.
 *
 * @author IYTE Yazılım Topluluğu
 * @since 2026-04-04
 */
public class IllegalUserStateException extends DomainException {

    public IllegalUserStateException(String message) {
        super(ErrorCode.ILLEGAL_USER_STATE, message);
    }
}
