package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;

public class FlaggedContentNotFoundException extends DomainException {
    public FlaggedContentNotFoundException(String flagId) {
        super(ErrorCode.FLAGGED_CONTENT_NOT_FOUND, "Flagged content not found: " + flagId);
    }
}
