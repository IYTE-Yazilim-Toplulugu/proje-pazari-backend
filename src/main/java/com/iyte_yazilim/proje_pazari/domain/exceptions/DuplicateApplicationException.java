package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

/** Raised when concurrent submissions target the same project and applicant. */
public class DuplicateApplicationException extends DomainException {

    public DuplicateApplicationException(String projectId, String userId, Throwable cause) {
        super(
                ErrorCode.APPLICATION_ALREADY_EXISTS,
                "Application already exists for project " + projectId + " and user " + userId,
                cause);
    }
}
