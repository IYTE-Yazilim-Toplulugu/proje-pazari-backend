package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.services.ApplicationReviewService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Runs one bulk-review item in its own transaction so other items can commit independently. */
@Component
@RequiredArgsConstructor
public class BulkApplicationReviewItemProcessor {

    private final ApplicationReviewService reviewService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ReviewApplicationCommandResult process(
            String applicationId, ApplicationStatus status, String reviewMessage) {
        return reviewService.review(applicationId, status, reviewMessage);
    }
}
