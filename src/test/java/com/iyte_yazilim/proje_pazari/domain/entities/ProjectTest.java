package com.iyte_yazilim.proje_pazari.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ProjectTest {

    @Nested
    @DisplayName("State Machine Tests - transitionTo()")
    class TransitionToTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException when transitionTo is called with null")
        void transitionTo_null_throwsException() {
            Project project = new Project();
            assertThatThrownBy(() -> project.transitionTo(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("New status cannot be null");
        }

        // test DRAFT -> OPEN
        @Test
        @DisplayName("Should allow transition from DRAFT to OPEN")
        void transitionTo_draftToOpen() {
            Project project = new Project();
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.DRAFT);
            project.transitionTo(ProjectStatus.OPEN);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPEN);
        }

        // test DRAFT -> CANCELLED
        @Test
        @DisplayName("Should allow transition from DRAFT to CANCELLED")
        void transitionTo_draftToCancelled() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.CANCELLED);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
        }

        // test OPEN -> CANCELLED
        @Test
        @DisplayName("Should allow transition from OPEN to CANCELLED")
        void transitionTo_openToCancelled() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);
            project.transitionTo(ProjectStatus.CANCELLED);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
        }

        // test IN_PROGRESS -> COMPLETED
        @Test
        @DisplayName("Should allow transition from IN_PROGRESS to COMPLETED")
        void transitionTo_inProgressToCompleted() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);
            project.transitionTo(ProjectStatus.IN_PROGRESS);
            project.transitionTo(ProjectStatus.COMPLETED);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
        }

        // test COMPLETED -> anything -> IllegalStateException
        @Test
        @DisplayName("Should thrown IllegalStateException when transitioning from COMPLETED")
        void transitionTo_completedToAnything_throwsException() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);
            project.transitionTo(ProjectStatus.IN_PROGRESS);
            project.transitionTo(ProjectStatus.COMPLETED);

            assertThatThrownBy(() -> project.transitionTo(ProjectStatus.OPEN))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid state transition");
        }

        // test CANCELLED -> anything -> IllegalStateException
        @Test
        @DisplayName("Should thrown IllegalStateException when transitioning from CANCELLED")
        void transitionTo_cancelledToAnything_throwsException() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.CANCELLED);

            assertThatThrownBy(() -> project.transitionTo(ProjectStatus.OPEN))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid state transition");
        }

        // test same-status transition throws IllegalStateException
        @Test
        @DisplayName("Should throw IllegalStateException for same-status transition")
        void transitionTo_sameStatus_throwsException() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);

            assertThatThrownBy(() -> project.transitionTo(ProjectStatus.OPEN))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid state transition");
        }

        // test DRAFT -> IN_PROGRESS (invalid)
        @Test
        @DisplayName("Should throw IllegalStateException for DRAFT to IN_PROGRESS")
        void transitionTo_draftToInProgress_throwsException() {
            Project project = new Project();

            assertThatThrownBy(() -> project.transitionTo(ProjectStatus.IN_PROGRESS))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid state transition");
        }

        // test OPEN -> IN_PROGRESS (valid)
        @Test
        @DisplayName("Should allow transition from OPEN to IN_PROGRESS")
        void transitionTo_openToInProgress() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);
            project.transitionTo(ProjectStatus.IN_PROGRESS);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
        }

        // test OPEN -> COMPLETED (invalid)
        @Test
        @DisplayName("Should throw IllegalStateException for OPEN to COMPLETED")
        void transitionTo_openToCompleted_throwsException() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);

            assertThatThrownBy(() -> project.transitionTo(ProjectStatus.COMPLETED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid state transition");
        }

        // test IN_PROGRESS -> CANCELLED (valid)
        @Test
        @DisplayName("Should allow transition from IN_PROGRESS to CANCELLED")
        void transitionTo_inProgressToCancelled() {
            Project project = new Project();
            project.transitionTo(ProjectStatus.OPEN);
            project.transitionTo(ProjectStatus.IN_PROGRESS);
            project.transitionTo(ProjectStatus.CANCELLED);
            assertThat(project.getStatus()).isEqualTo(ProjectStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Capacity Encapsulation Tests")
    class CapacityEncapsulationTests {

        @Test
        @DisplayName("incrementTeamSize() handles infinite capacity when maxTeamSize is null")
        void incrementTeamSize_nullMaxTeamSize_incrementsSuccessfully() {
            Project project = new Project(); // default maxTeamSize is null
            project.incrementTeamSize();
            assertThat(project.getCurrentTeamSize()).isEqualTo(1);
            project.incrementTeamSize();
            assertThat(project.getCurrentTeamSize()).isEqualTo(2);
        }

        @Test
        @DisplayName("incrementTeamSize() throws IllegalStateException when project is full")
        void incrementTeamSize_projectIsFull_throwsException() {
            Project project = new Project();
            project.setMaxTeamSize(1);
            project.incrementTeamSize();

            assertThatThrownBy(project::incrementTeamSize)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Project is at maximum capacity");
        }

        @Test
        @DisplayName("isFull() returns false when maxTeamSize is null")
        void isFull_nullMaxTeamSize_returnsFalse() {
            Project project = new Project();
            assertThat(project.isFull()).isFalse();
        }

        @Test
        @DisplayName("isFull() returns true when currentTeamSize equals maxTeamSize")
        void isFull_atCapacity_returnsTrue() {
            Project project = new Project();
            project.setMaxTeamSize(2);
            project.incrementTeamSize();
            project.incrementTeamSize();
            assertThat(project.isFull()).isTrue();
        }

        @Test
        @DisplayName("canAcceptApplications() returns true when OPEN and not full")
        void canAcceptApplications_openAndNotFull_returnsTrue() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.OPEN, 0);
            project.setMaxTeamSize(5);
            assertThat(project.canAcceptApplications()).isTrue();
        }

        @Test
        @DisplayName("canAcceptApplications() returns false when DRAFT")
        void canAcceptApplications_draft_returnsFalse() {
            Project project = new Project();
            assertThat(project.canAcceptApplications()).isFalse();
        }

        @Test
        @DisplayName("canAcceptApplications() returns false when OPEN but full")
        void canAcceptApplications_openButFull_returnsFalse() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.OPEN, 5);
            project.setMaxTeamSize(5);
            assertThat(project.canAcceptApplications()).isFalse();
        }
    }

    @Nested
    @DisplayName("Lifecycle Guards Tests")
    class LifecycleGuardsTests {

        @Test
        @DisplayName("canBeDeleted() returns true for DRAFT status")
        void canBeDeleted_draft_returnsTrue() {
            Project project = new Project();
            assertThat(project.canBeDeleted()).isTrue();
        }

        @Test
        @DisplayName("canBeDeleted() returns false for IN_PROGRESS status")
        void canBeDeleted_inProgress_returnsFalse() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.IN_PROGRESS, 0);
            assertThat(project.canBeDeleted()).isFalse();
        }

        @Test
        @DisplayName("canBeUpdated() returns true for OPEN status")
        void canBeUpdated_open_returnsTrue() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.OPEN, 0);
            assertThat(project.canBeUpdated()).isTrue();
        }

        @Test
        @DisplayName("canBeUpdated() returns false for COMPLETED status")
        void canBeUpdated_completed_returnsFalse() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.COMPLETED, 0);
            assertThat(project.canBeUpdated()).isFalse();
        }

        @Test
        @DisplayName("canBeUpdated() returns false for CANCELLED status")
        void canBeUpdated_cancelled_returnsFalse() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.CANCELLED, 0);
            assertThat(project.canBeUpdated()).isFalse();
        }
    }

    @Nested
    @DisplayName("Infrastructure/Mapping Hook Tests")
    class ReconstituteTests {

        @Test
        @DisplayName("reconstitute with null currentTeamSize handles gracefully")
        void reconstitute_nullCurrentTeamSize_defaultsToZero() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.OPEN, null);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.OPEN);
            assertThat(project.getCurrentTeamSize()).isEqualTo(0);
        }

        @Test
        @DisplayName("reconstitute sets status and currentTeamSize correctly")
        void reconstitute_setsFieldsCorrectly() {
            Project project = new Project();
            project.reconstitute(ProjectStatus.IN_PROGRESS, 3);

            assertThat(project.getStatus()).isEqualTo(ProjectStatus.IN_PROGRESS);
            assertThat(project.getCurrentTeamSize()).isEqualTo(3);
        }
    }
}
