package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.IRequestHandler;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationReviewException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalApplicationStateException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkApplicationActionHandler
        implements IRequestHandler<BulkApplicationActionCommand, ApiResponse<BulkActionResult>> {

    private final BulkApplicationReviewItemProcessor itemProcessor;

    @Override
    public ApiResponse<BulkActionResult> handle(BulkApplicationActionCommand command) {
        BulkActionResult result = new BulkActionResult();

        if (command.applicationIds() == null || command.applicationIds().isEmpty()) {
            return ApiResponse.validationError("Application IDs list cannot be empty");
        }

        ApplicationStatus status = reviewStatus(command.action());
        if (status == null) {
            command.applicationIds().forEach(id -> result.addFailure(id, "UNKNOWN_ACTION"));
            return ApiResponse.success(result, "Bulk application action completed");
        }

        for (String applicationId : command.applicationIds()) {
            processItem(result, applicationId, status);
        }

        return ApiResponse.success(result, "Bulk application action completed");
    }

    private void processItem(
            BulkActionResult result, String applicationId, ApplicationStatus status) {
        try {
            itemProcessor.process(applicationId, status, null);
            result.incrementSuccess();
        } catch (ApplicationNotFoundException exception) {
            result.addFailure(applicationId, "APPLICATION_NOT_FOUND");
        } catch (ApplicationReviewException exception) {
            result.addFailure(applicationId, exception.getFailure().name());
        } catch (IllegalApplicationStateException exception) {
            result.addFailure(applicationId, "APPLICATION_NOT_PENDING");
        } catch (RuntimeException exception) {
            log.warn(
                    "Unexpected bulk application review failure for applicationId={}",
                    applicationId,
                    exception);
            result.addFailure(applicationId, "APPLICATION_REVIEW_FAILED");
        }
    }

    private ApplicationStatus reviewStatus(String action) {
        if (action == null) {
            return null;
        }
        return switch (action.toUpperCase(Locale.ROOT)) {
            case "APPROVE" -> ApplicationStatus.APPROVED;
            case "REJECT" -> ApplicationStatus.REJECTED;
            default -> null;
        };
    }
}
