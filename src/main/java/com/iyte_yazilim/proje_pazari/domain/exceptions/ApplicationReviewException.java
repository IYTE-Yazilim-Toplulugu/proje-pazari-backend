package com.iyte_yazilim.proje_pazari.domain.exceptions;

import com.iyte_yazilim.proje_pazari.application.common.ErrorCode;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationReviewFailure;
import lombok.Getter;

/** Raised when project state or a concurrent update prevents an application review. */
@Getter
public class ApplicationReviewException extends DomainException {

    private final ApplicationReviewFailure failure;

    public ApplicationReviewException(ApplicationReviewFailure failure) {
        this(failure, null);
    }

    public ApplicationReviewException(ApplicationReviewFailure failure, Throwable cause) {
        super(errorCodeFor(failure), "Application review failed: " + failure, cause);
        this.failure = failure;
    }

    private static ErrorCode errorCodeFor(ApplicationReviewFailure failure) {
        return failure == ApplicationReviewFailure.CONCURRENT_REVIEW_CONFLICT
                ? ErrorCode.APPLICATION_REVIEW_CONFLICT
                : ErrorCode.PROJECT_CANNOT_ACCEPT_APPLICATIONS;
    }
}
