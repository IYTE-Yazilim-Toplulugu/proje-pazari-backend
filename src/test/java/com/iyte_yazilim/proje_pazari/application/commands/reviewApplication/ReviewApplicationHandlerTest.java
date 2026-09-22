package com.iyte_yazilim.proje_pazari.application.commands.reviewApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.iyte_yazilim.proje_pazari.application.common.ApiResponse;
import com.iyte_yazilim.proje_pazari.application.common.ResponseCode;
import com.iyte_yazilim.proje_pazari.application.services.ApplicationReviewService;
import com.iyte_yazilim.proje_pazari.application.services.MessageService;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationReviewFailure;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationNotFoundException;
import com.iyte_yazilim.proje_pazari.domain.exceptions.ApplicationReviewException;
import com.iyte_yazilim.proje_pazari.domain.models.results.ReviewApplicationCommandResult;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewApplicationHandlerTest {

    private static final String APPLICATION_ID = "01APPLICATION000000000000";
    private static final String PROJECT_ID = "01PROJECT00000000000000000";
    private static final String OWNER_ID = "01OWNER000000000000000000";
    private static final String APPLICANT_ID = "01APPLICANT00000000000000";

    @Mock private ProjectApplicationRepository applicationRepository;
    @Mock private ApplicationReviewService reviewService;
    @Mock private MessageService messageService;

    @InjectMocks private ReviewApplicationHandler handler;

    private ProjectApplicationEntity application;

    @BeforeEach
    void setUp() {
        UserEntity owner = new UserEntity();
        owner.setId(OWNER_ID);
        ProjectEntity project = new ProjectEntity();
        project.setId(PROJECT_ID);
        project.setOwner(owner);

        application = new ProjectApplicationEntity();
        application.setId(APPLICATION_ID);
        application.setProject(project);
        application.setStatus(ApplicationStatus.PENDING);

        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.of(application));
    }

    @Test
    void ownerApprovalUsesTheSharedReviewOperation() {
        ReviewApplicationCommandResult result =
                new ReviewApplicationCommandResult(
                        APPLICATION_ID, PROJECT_ID, "Shared Review", "APPROVED");
        when(reviewService.review(APPLICATION_ID, ApplicationStatus.APPROVED, "Welcome"))
                .thenReturn(result);

        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        command(OWNER_ID, RoleType.USER, ApplicationStatus.APPROVED, "Welcome"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        assertThat(response.getData()).isSameAs(result);
        verify(reviewService).review(APPLICATION_ID, ApplicationStatus.APPROVED, "Welcome");
    }

    @Test
    void rejectionUsesTheSameSharedReviewOperation() {
        ReviewApplicationCommandResult result =
                new ReviewApplicationCommandResult(
                        APPLICATION_ID, PROJECT_ID, "Shared Review", "REJECTED");
        when(reviewService.review(APPLICATION_ID, ApplicationStatus.REJECTED, "No fit"))
                .thenReturn(result);

        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        command(OWNER_ID, RoleType.USER, ApplicationStatus.REJECTED, "No fit"));

        assertThat(response.getData()).isSameAs(result);
        verify(reviewService).review(APPLICATION_ID, ApplicationStatus.REJECTED, "No fit");
    }

    @Test
    void administratorCanUseTheSharedReviewOperation() {
        ReviewApplicationCommandResult result =
                new ReviewApplicationCommandResult(
                        APPLICATION_ID, PROJECT_ID, "Shared Review", "APPROVED");
        when(reviewService.review(APPLICATION_ID, ApplicationStatus.APPROVED, null))
                .thenReturn(result);

        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        command(APPLICANT_ID, RoleType.ADMIN, ApplicationStatus.APPROVED, null));

        assertThat(response.getCode()).isEqualTo(ResponseCode.SUCCESS);
        verify(reviewService).review(APPLICATION_ID, ApplicationStatus.APPROVED, null);
    }

    @Test
    void unauthorizedRequesterCannotReachTheSharedReviewOperation() {
        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(
                        command(APPLICANT_ID, RoleType.USER, ApplicationStatus.APPROVED, "Forged"));

        assertThat(response.getCode()).isEqualTo(ResponseCode.FORBIDDEN);
        verify(reviewService, never()).review(APPLICATION_ID, ApplicationStatus.APPROVED, "Forged");
    }

    @Test
    void invalidReviewStatusCannotReachTheSharedReviewOperation() {
        ApiResponse<ReviewApplicationCommandResult> response =
                handler.handle(command(OWNER_ID, RoleType.USER, ApplicationStatus.WITHDRAWN, null));

        assertThat(response.getCode()).isEqualTo(ResponseCode.BAD_REQUEST);
        verify(reviewService, never()).review(APPLICATION_ID, ApplicationStatus.WITHDRAWN, null);
    }

    @Test
    void missingApplicationStillUsesTheExistingNotFoundContract() {
        when(applicationRepository.findById(APPLICATION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                handler.handle(
                                        command(
                                                OWNER_ID,
                                                RoleType.USER,
                                                ApplicationStatus.APPROVED,
                                                null)))
                .isInstanceOf(ApplicationNotFoundException.class);
        verify(reviewService, never()).review(APPLICATION_ID, ApplicationStatus.APPROVED, null);
    }

    @Test
    void sharedReviewFailurePropagatesToTheStandardExceptionContract() {
        ApplicationReviewException failure =
                new ApplicationReviewException(ApplicationReviewFailure.PROJECT_FULL);
        when(reviewService.review(APPLICATION_ID, ApplicationStatus.APPROVED, null))
                .thenThrow(failure);

        assertThatThrownBy(
                        () ->
                                handler.handle(
                                        command(
                                                OWNER_ID,
                                                RoleType.USER,
                                                ApplicationStatus.APPROVED,
                                                null)))
                .isSameAs(failure);
    }

    private static ReviewApplicationCommand command(
            String requesterId, RoleType role, ApplicationStatus status, String reviewMessage) {
        return new ReviewApplicationCommand(
                APPLICATION_ID, requesterId, role, status, reviewMessage);
    }
}
