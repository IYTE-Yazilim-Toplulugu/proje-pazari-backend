package com.iyte_yazilim.proje_pazari.domain.entities;

import static org.junit.jupiter.api.Assertions.*;

import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.exceptions.IllegalApplicationStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ProjectApplicationTest {

    private ProjectApplication application;

    @BeforeEach
    void setUp() {
        application = new ProjectApplication();
    }

    // ---------------------------------------------------------------------------
    //  Constructor & Defaults
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("Default constructor sets status to PENDING")
    void defaultConstructor_setsStatusToPending() {
        assertEquals(ApplicationStatus.PENDING, application.getStatus());
    }

    @Test
    @DisplayName("Parameterized constructor sets project, user, and PENDING status")
    void parameterizedConstructor_setsFieldsCorrectly() {
        Project project = new Project();
        User user = new User("a@test.com", "pw", "A", "B");

        ProjectApplication app = new ProjectApplication(project, user);

        assertSame(project, app.getProject());
        assertSame(user, app.getUser());
        assertEquals(ApplicationStatus.PENDING, app.getStatus());
    }

    // ---------------------------------------------------------------------------
    //  approve()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("approve()")
    class ApproveTests {

        @Test
        @DisplayName("Approves a PENDING application")
        void approve_fromPending_succeeds() {
            application.approve();
            assertEquals(ApplicationStatus.APPROVED, application.getStatus());
        }

        @ParameterizedTest(name = "Throws when current status is {0}")
        @EnumSource(
                value = ApplicationStatus.class,
                names = {"APPROVED", "REJECTED", "WITHDRAWN"})
        @DisplayName("Throws IllegalApplicationStateException on non-PENDING status")
        void approve_fromNonPending_throwsException(ApplicationStatus initialStatus) {
            application.reconstitute(null, null, initialStatus);

            IllegalApplicationStateException ex =
                    assertThrows(
                            IllegalApplicationStateException.class, () -> application.approve());
            assertTrue(ex.getMessage().contains("approve"));
            assertTrue(ex.getMessage().contains(initialStatus.toString()));
        }
    }

    // ---------------------------------------------------------------------------
    //  reject()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("reject()")
    class RejectTests {

        @Test
        @DisplayName("Rejects a PENDING application")
        void reject_fromPending_succeeds() {
            application.reject();
            assertEquals(ApplicationStatus.REJECTED, application.getStatus());
        }

        @ParameterizedTest(name = "Throws when current status is {0}")
        @EnumSource(
                value = ApplicationStatus.class,
                names = {"APPROVED", "REJECTED", "WITHDRAWN"})
        @DisplayName("Throws IllegalApplicationStateException on non-PENDING status")
        void reject_fromNonPending_throwsException(ApplicationStatus initialStatus) {
            application.reconstitute(null, null, initialStatus);

            IllegalApplicationStateException ex =
                    assertThrows(
                            IllegalApplicationStateException.class, () -> application.reject());
            assertTrue(ex.getMessage().contains("reject"));
            assertTrue(ex.getMessage().contains(initialStatus.toString()));
        }
    }

    // ---------------------------------------------------------------------------
    //  withdraw()
    // ---------------------------------------------------------------------------

    @Nested
    @DisplayName("withdraw()")
    class WithdrawTests {

        @Test
        @DisplayName("Withdraws a PENDING application")
        void withdraw_fromPending_succeeds() {
            application.withdraw();
            assertEquals(ApplicationStatus.WITHDRAWN, application.getStatus());
        }

        @ParameterizedTest(name = "Throws when current status is {0}")
        @EnumSource(
                value = ApplicationStatus.class,
                names = {"APPROVED", "REJECTED", "WITHDRAWN"})
        @DisplayName("Throws IllegalApplicationStateException on non-PENDING status")
        void withdraw_fromNonPending_throwsException(ApplicationStatus initialStatus) {
            application.reconstitute(null, null, initialStatus);

            IllegalApplicationStateException ex =
                    assertThrows(
                            IllegalApplicationStateException.class, () -> application.withdraw());
            assertTrue(ex.getMessage().contains("withdraw"));
            assertTrue(ex.getMessage().contains(initialStatus.toString()));
        }
    }

    // ---------------------------------------------------------------------------
    //  reconstitute() — infrastructure-only
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("reconstitute() sets state without triggering guards")
    void reconstitute_setsStateDirectly() {
        Project project = new Project();
        User user = new User("b@test.com", "pw", "B", "C");

        application.reconstitute(project, user, ApplicationStatus.APPROVED);

        assertSame(project, application.getProject());
        assertSame(user, application.getUser());
        assertEquals(ApplicationStatus.APPROVED, application.getStatus());
    }

    @Test
    @DisplayName("reconstitute() then approve() throws since status is not PENDING")
    void reconstitute_thenApprove_throwsIfNotPending() {
        application.reconstitute(null, null, ApplicationStatus.REJECTED);

        assertThrows(IllegalApplicationStateException.class, () -> application.approve());
    }

    // ---------------------------------------------------------------------------
    //  Double transition prevention
    // ---------------------------------------------------------------------------

    @Test
    @DisplayName("Cannot approve an already approved application")
    void approve_afterApprove_throws() {
        application.approve();
        assertThrows(IllegalApplicationStateException.class, () -> application.approve());
    }

    @Test
    @DisplayName("Cannot reject an already rejected application")
    void reject_afterReject_throws() {
        application.reject();
        assertThrows(IllegalApplicationStateException.class, () -> application.reject());
    }

    @Test
    @DisplayName("Cannot withdraw an already withdrawn application")
    void withdraw_afterWithdraw_throws() {
        application.withdraw();
        assertThrows(IllegalApplicationStateException.class, () -> application.withdraw());
    }

    @Test
    @DisplayName("Cannot reject after approve")
    void reject_afterApprove_throws() {
        application.approve();
        assertThrows(IllegalApplicationStateException.class, () -> application.reject());
    }

    @Test
    @DisplayName("Cannot withdraw after reject")
    void withdraw_afterReject_throws() {
        application.reject();
        assertThrows(IllegalApplicationStateException.class, () -> application.withdraw());
    }
}
