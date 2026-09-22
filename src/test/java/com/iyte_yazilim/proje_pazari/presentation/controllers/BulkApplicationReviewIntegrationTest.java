package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.domain.events.ApplicationReviewedEvent;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MvcResult;

@RecordApplicationEvents
class BulkApplicationReviewIntegrationTest extends IntegrationTestBase {

    private static final String BULK_URL = "/api/v1/admin/applications/bulk-action";
    private static final AtomicInteger EMAIL_SEQUENCE = new AtomicInteger();

    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectApplicationRepository applicationRepository;
    @Autowired private JwtUtil jwtUtil;
    @Autowired private ApplicationEvents applicationEvents;

    private final ExecutorService requestExecutor = Executors.newFixedThreadPool(2);
    private String adminToken;

    @BeforeEach
    void createAdminToken() {
        adminToken = jwtUtil.generateToken("admin-id", "admin@test.dev", "ADMIN");
    }

    @AfterEach
    void stopRequestExecutor() {
        requestExecutor.shutdownNow();
    }

    @Test
    void oneProjectBatchCannotApproveBeyondCapacity() throws Exception {
        Fixture fixture = project(ProjectStatus.OPEN, 1, 2, LocalDateTime.now().plusDays(1), 2);

        performBulk(applicationIds(fixture))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.failures[0].reason").value("PROJECT_FULL"));

        assertThat(
                        projectRepository
                                .findById(fixture.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(2);
        assertThat(applicationRepository.findByProjectId(fixture.project().getId()))
                .extracting(ProjectApplicationEntity::getStatus)
                .containsExactlyInAnyOrder(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
        assertThat(applicationEvents.stream(ApplicationReviewedEvent.class)).hasSize(1);
    }

    @Test
    void mixedProjectsUpdateTheirOwnTeamCountsAndPublishOneEventPerSuccess() throws Exception {
        Fixture first = project(ProjectStatus.OPEN, 1, 3, LocalDateTime.now().plusDays(1), 1);
        Fixture second = project(ProjectStatus.OPEN, 2, 4, LocalDateTime.now().plusDays(1), 1);

        performBulk(
                        List.of(
                                first.applications().getFirst().getId(),
                                second.applications().getFirst().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(2))
                .andExpect(jsonPath("$.data.failureCount").value(0));

        assertThat(
                        projectRepository
                                .findById(first.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(2);
        assertThat(
                        projectRepository
                                .findById(second.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(3);
        assertThat(applicationEvents.stream(ApplicationReviewedEvent.class)).hasSize(2);
    }

    @Test
    void bulkRejectionPublishesTheSharedReviewEventWithoutChangingTeamSize() throws Exception {
        Fixture fixture = project(ProjectStatus.OPEN, 1, 3, null, 1);

        performBulk("REJECT", applicationIds(fixture))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(0));

        assertThat(
                        projectRepository
                                .findById(fixture.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(1);
        assertThat(
                        applicationRepository
                                .findById(fixture.applications().getFirst().getId())
                                .orElseThrow()
                                .getStatus())
                .isEqualTo(ApplicationStatus.REJECTED);
        assertThat(applicationEvents.stream(ApplicationReviewedEvent.class))
                .singleElement()
                .extracting(ApplicationReviewedEvent::status)
                .isEqualTo(ApplicationStatus.REJECTED);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("ineligibleProjectStates")
    void ineligibleProjectsFailWithoutMutationOrEvents(
            String scenario,
            ProjectStatus status,
            int currentTeamSize,
            int maxTeamSize,
            LocalDateTime deadline,
            String expectedReason)
            throws Exception {
        Fixture fixture = project(status, currentTeamSize, maxTeamSize, deadline, 1);

        performBulk(applicationIds(fixture))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(0))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.failures[0].reason").value(expectedReason));

        assertThat(
                        projectRepository
                                .findById(fixture.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(currentTeamSize);
        assertThat(
                        applicationRepository
                                .findById(fixture.applications().getFirst().getId())
                                .orElseThrow()
                                .getStatus())
                .isEqualTo(ApplicationStatus.PENDING);
        assertThat(applicationEvents.stream(ApplicationReviewedEvent.class)).isEmpty();
    }

    @Test
    void failedItemRollsBackButLaterItemCommits() throws Exception {
        Fixture closed = project(ProjectStatus.CANCELLED, 1, 3, null, 1);
        Fixture open = project(ProjectStatus.OPEN, 1, 3, null, 1);

        performBulk(
                        List.of(
                                closed.applications().getFirst().getId(),
                                open.applications().getFirst().getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.successCount").value(1))
                .andExpect(jsonPath("$.data.failureCount").value(1))
                .andExpect(jsonPath("$.data.failures[0].reason").value("PROJECT_NOT_OPEN"));

        assertThat(
                        applicationRepository
                                .findById(closed.applications().getFirst().getId())
                                .orElseThrow()
                                .getStatus())
                .isEqualTo(ApplicationStatus.PENDING);
        assertThat(
                        applicationRepository
                                .findById(open.applications().getFirst().getId())
                                .orElseThrow()
                                .getStatus())
                .isEqualTo(ApplicationStatus.APPROVED);
        assertThat(
                        projectRepository
                                .findById(open.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(2);
        assertThat(applicationEvents.stream(ApplicationReviewedEvent.class)).hasSize(1);
    }

    @Test
    void concurrentBulkApprovalsCannotOverfillAProject() throws Exception {
        Fixture fixture = project(ProjectStatus.OPEN, 1, 2, null, 2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<MvcResult>> requests = new ArrayList<>();

        for (ProjectApplicationEntity application : fixture.applications()) {
            requests.add(
                    requestExecutor.submit(
                            () -> {
                                ready.countDown();
                                start.await();
                                return performBulk(List.of(application.getId())).andReturn();
                            }));
        }

        assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        List<MvcResult> results = new ArrayList<>();
        for (Future<MvcResult> request : requests) {
            results.add(request.get(45, TimeUnit.SECONDS));
        }

        assertThat(results)
                .extracting(result -> result.getResponse().getStatus())
                .containsOnly(200);
        int successes = 0;
        int failures = 0;
        for (MvcResult result : results) {
            var response = objectMapper.readTree(result.getResponse().getContentAsString());
            successes += response.get("data").get("successCount").asInt();
            failures += response.get("data").get("failureCount").asInt();
        }
        assertThat(successes).isEqualTo(1);
        assertThat(failures).isEqualTo(1);
        assertThat(
                        projectRepository
                                .findById(fixture.project().getId())
                                .orElseThrow()
                                .getCurrentTeamSize())
                .isEqualTo(2);
        assertThat(applicationRepository.findByProjectId(fixture.project().getId()))
                .extracting(ProjectApplicationEntity::getStatus)
                .containsExactlyInAnyOrder(ApplicationStatus.APPROVED, ApplicationStatus.PENDING);
    }

    private org.springframework.test.web.servlet.ResultActions performBulk(List<String> ids)
            throws Exception {
        return performBulk("APPROVE", ids);
    }

    private org.springframework.test.web.servlet.ResultActions performBulk(
            String action, List<String> ids) throws Exception {
        return mockMvc.perform(
                post(BULK_URL)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        Map.of("action", action, "applicationIds", ids))));
    }

    private Fixture project(
            ProjectStatus status,
            int currentTeamSize,
            int maxTeamSize,
            LocalDateTime deadline,
            int applicationCount) {
        UserEntity owner = user("owner");
        userRepository.saveAndFlush(owner);

        ProjectEntity project = new ProjectEntity();
        project.setTitle("Bulk Review " + EMAIL_SEQUENCE.incrementAndGet());
        project.setOwner(owner);
        project.setStatus(status);
        project.setCurrentTeamSize(currentTeamSize);
        project.setMaxTeamSize(maxTeamSize);
        project.setDeadline(deadline);
        project = projectRepository.saveAndFlush(project);

        List<ProjectApplicationEntity> applications = new ArrayList<>();
        for (int index = 0; index < applicationCount; index++) {
            UserEntity applicant = user("applicant");
            userRepository.saveAndFlush(applicant);
            ProjectApplicationEntity application = new ProjectApplicationEntity();
            application.setProject(project);
            application.setUser(applicant);
            application.setStatus(ApplicationStatus.PENDING);
            applications.add(applicationRepository.saveAndFlush(application));
        }
        return new Fixture(project, applications);
    }

    private static List<String> applicationIds(Fixture fixture) {
        return fixture.applications().stream().map(ProjectApplicationEntity::getId).toList();
    }

    private static Stream<Arguments> ineligibleProjectStates() {
        return Stream.of(
                Arguments.of("non-open", ProjectStatus.IN_PROGRESS, 1, 3, null, "PROJECT_NOT_OPEN"),
                Arguments.of(
                        "expired",
                        ProjectStatus.OPEN,
                        1,
                        3,
                        LocalDateTime.now().minusDays(1),
                        "PROJECT_APPLICATION_DEADLINE_PASSED"),
                Arguments.of("full", ProjectStatus.OPEN, 3, 3, null, "PROJECT_FULL"));
    }

    private static UserEntity user(String prefix) {
        int sequence = EMAIL_SEQUENCE.incrementAndGet();
        UserEntity user = new UserEntity();
        user.setEmail(prefix + sequence + "@test.dev");
        user.setPassword("encoded-password");
        user.setFirstName(prefix);
        user.setLastName("Review");
        return user;
    }

    private record Fixture(ProjectEntity project, List<ProjectApplicationEntity> applications) {}
}
