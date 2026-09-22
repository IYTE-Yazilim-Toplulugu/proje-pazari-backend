package com.iyte_yazilim.proje_pazari.application.commands.bulkApplicationAction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.dtos.BulkActionResult;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationReviewFailure;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationReviewException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalApplicationStateException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BulkApplicationActionHandlerTest {

    private static final String FIRST_ID = "01APPLICATION000000000001";
    private static final String SECOND_ID = "01APPLICATION000000000002";

    @Mock private BulkApplicationReviewItemProcessor itemProcessor;

    @InjectMocks private BulkApplicationActionHandler handler;

    @Test
    void approveRoutesEveryItemThroughTheTransactionalSharedReviewPath() {
        ApiResponse<BulkActionResult> response =
                handler.handle(
                        new BulkApplicationActionCommand("approve", List.of(FIRST_ID, SECOND_ID)));

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getData().getSuccessCount()).isEqualTo(2);
        assertThat(response.getData().getFailureCount()).isZero();
        verify(itemProcessor).process(FIRST_ID, ApplicationStatus.APPROVED, null);
        verify(itemProcessor).process(SECOND_ID, ApplicationStatus.APPROVED, null);
    }

    @Test
    void rejectUsesTheSameSharedReviewPath() {
        ApiResponse<BulkActionResult> response =
                handler.handle(new BulkApplicationActionCommand("REJECT", List.of(FIRST_ID)));

        assertThat(response.getData().getSuccessCount()).isOne();
        verify(itemProcessor).process(FIRST_ID, ApplicationStatus.REJECTED, null);
    }

    @Test
    void missingAndIneligibleItemsRemainVisibleAsDeterministicPartialFailures() {
        when(itemProcessor.process(FIRST_ID, ApplicationStatus.APPROVED, null))
                .thenThrow(new ApplicationNotFoundException(FIRST_ID));
        when(itemProcessor.process(SECOND_ID, ApplicationStatus.APPROVED, null))
                .thenThrow(new ApplicationReviewException(ApplicationReviewFailure.PROJECT_FULL));

        ApiResponse<BulkActionResult> response =
                handler.handle(
                        new BulkApplicationActionCommand("APPROVE", List.of(FIRST_ID, SECOND_ID)));

        assertThat(response.getData().getSuccessCount()).isZero();
        assertThat(response.getData().getFailureCount()).isEqualTo(2);
        assertThat(response.getData().getFailures())
                .extracting(BulkActionResult.FailureDetail::reason)
                .containsExactly("APPLICATION_NOT_FOUND", "PROJECT_FULL");
    }

    @Test
    void nonPendingItemHasAStableFailureAndLaterItemsStillSucceed() {
        when(itemProcessor.process(FIRST_ID, ApplicationStatus.APPROVED, null))
                .thenThrow(
                        new IllegalApplicationStateException(
                                "approve", ApplicationStatus.APPROVED));

        ApiResponse<BulkActionResult> response =
                handler.handle(
                        new BulkApplicationActionCommand("APPROVE", List.of(FIRST_ID, SECOND_ID)));

        assertThat(response.getData().getSuccessCount()).isOne();
        assertThat(response.getData().getFailures())
                .singleElement()
                .extracting(BulkActionResult.FailureDetail::reason)
                .isEqualTo("APPLICATION_NOT_PENDING");
        verify(itemProcessor).process(SECOND_ID, ApplicationStatus.APPROVED, null);
    }

    @Test
    void unknownActionFailsEveryItemWithoutStartingTransactions() {
        ApiResponse<BulkActionResult> response =
                handler.handle(
                        new BulkApplicationActionCommand("ARCHIVE", List.of(FIRST_ID, SECOND_ID)));

        assertThat(response.getData().getSuccessCount()).isZero();
        assertThat(response.getData().getFailureCount()).isEqualTo(2);
        assertThat(response.getData().getFailures())
                .extracting(BulkActionResult.FailureDetail::reason)
                .containsOnly("UNKNOWN_ACTION");
        verify(itemProcessor, never()).process(FIRST_ID, ApplicationStatus.APPROVED, null);
        verify(itemProcessor, never()).process(FIRST_ID, ApplicationStatus.REJECTED, null);
    }

    @Test
    void emptyApplicationListReturnsValidationError() {
        ApiResponse<BulkActionResult> response =
                handler.handle(new BulkApplicationActionCommand("APPROVE", List.of()));

        assertThat(response.getCode()).isEqualTo(ResponseCode.VALIDATION_ERROR);
        verify(itemProcessor, never()).process(FIRST_ID, ApplicationStatus.APPROVED, null);
    }
}
