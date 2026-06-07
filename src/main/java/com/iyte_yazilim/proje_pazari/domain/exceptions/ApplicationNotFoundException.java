package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class ApplicationNotFoundException extends DomainException {
    public ApplicationNotFoundException(String applicationId) {
        super(ErrorCode.APPLICATION_NOT_FOUND, "Application not found: " + applicationId);
    }
}
