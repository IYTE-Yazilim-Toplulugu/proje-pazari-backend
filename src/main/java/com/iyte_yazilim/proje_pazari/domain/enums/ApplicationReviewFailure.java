package com.iyte_yazilim.proje_pazari.domain.enums;

/** Stable reasons why an application review could not be completed. */
public enum ApplicationReviewFailure {
    PROJECT_NOT_OPEN,
    PROJECT_APPLICATION_DEADLINE_PASSED,
    PROJECT_FULL,
    CONCURRENT_REVIEW_CONFLICT
}
