package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class ApplicationControllerIntegrationTest extends IntegrationTestBase {

    private static final String OWNER_EMAIL = "owner@std.iyte.edu.tr";
    private static final String APPLICANT_EMAIL = "applicant@std.iyte.edu.tr";
    private static final String APPLICANT2_EMAIL = "applicant2@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";

    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectApplicationRepository applicationRepository;

    // ── Helper Methods ──────────────────────────────────────────────────

    private String getUserId(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private String createProjectOwnerAndGetToken() throws Exception {
        registerAndVerifyUser(OWNER_EMAIL, VALID_PASSWORD, "Ali", "Yilmaz");
        return loginAndGetToken(OWNER_EMAIL, VALID_PASSWORD);
    }

    private String createApplicantAndGetToken(String email, String firstName) throws Exception {
        registerAndVerifyUser(email, VALID_PASSWORD, firstName, "Demir");
        return loginAndGetToken(email, VALID_PASSWORD);
    }

    private Map<String, Object> validProjectData() {
        Map<String, Object> data = new HashMap<>();
        data.put("projectName", "AI Chatbot Project");
        data.put(
                "description",
                "Building an AI-powered chatbot for customer support using modern NLP techniques.");
        data.put("maxTeamSize", 5);
        data.put("requiredSkills", new String[] {"Python", "NLP", "Machine Learning"});
        data.put("category", "Artificial Intelligence");
        data.put("summary", "AI-powered chatbot for customer support using modern NLP techniques");
        return data;
    }

    private String createProjectAndGetId(String ownerToken) throws Exception {
        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/projects")
                                        .header("Authorization", "Bearer " + ownerToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        validProjectData())))
                        .andExpect(status().isCreated())
                        .andReturn();

        var jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        String projectId = jsonNode.get("data").get("projectId").asText();

        // --- FIX: Force the project to OPEN so our new domain rules allow applications! ---
        var project = projectRepository.findById(projectId).orElseThrow();
        project.setStatus(ProjectStatus.OPEN);
        projectRepository.save(project);
        // ----------------------------------------------------------------------------------

        return projectId;
    }

    private String submitApplicationUrl(String projectId) {
        return "/api/v1/projects/" + projectId + "/applications";
    }

    private String reviewApplicationUrl(String applicationId) {
        return "/api/v1/applications/" + applicationId + "/review";
    }

    private String projectApplicationsUrl(String projectId) {
        return "/api/v1/projects/" + projectId + "/applications";
    }

    private static final String MY_APPLICATIONS_URL = "/api/v1/users/me/applications";

    // ── 1. Submit Application Tests ─────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/projects/{projectId}/applications")
    class SubmitApplicationTests {

        private final ExecutorService requestExecutor = Executors.newFixedThreadPool(6);

        @AfterEach
        void stopRequestExecutor() {
            requestExecutor.shutdownNow();
        }

        @Test
        @DisplayName("1. Submit application to a project returns 201 CREATED")
        void submitApplication_returns201() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.applicationId").isNotEmpty())
                    .andExpect(jsonPath("$.data.projectId").value(projectId))
                    .andExpect(jsonPath("$.data.projectTitle").value("AI Chatbot Project"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("2. Application is persisted in database with PENDING status")
        void submitApplication_persistedInDatabase() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            var applications = applicationRepository.findByProjectId(projectId);
            assertThat(applications).hasSize(1);
            assertThat(applications.getFirst().getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(applications.getFirst().getUser().getEmail()).isEqualTo(APPLICANT_EMAIL);
        }

        @Test
        @DisplayName("3. Duplicate application returns 409 CONFLICT")
        void submitApplication_duplicate_returns409() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            // First application
            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            // Duplicate application
            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("APPLICATION_ALREADY_EXISTS"));
        }

        @Test
        @DisplayName("Project owner cannot apply to their own project")
        void submitApplication_asProjectOwner_returns403WithStableCode() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("SELF_APPLICATION_NOT_ALLOWED"));

            assertThat(applicationRepository.findByProjectId(projectId)).isEmpty();
        }

        @ParameterizedTest(name = "status {0}")
        @EnumSource(
                value = ProjectStatus.class,
                names = {"DRAFT", "IN_PROGRESS", "COMPLETED", "CANCELLED"})
        @DisplayName("Only OPEN projects accept applications")
        void submitApplication_toNonOpenProject_returns409WithStableCode(ProjectStatus status)
                throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            var project = projectRepository.findById(projectId).orElseThrow();
            project.setStatus(status);
            projectRepository.saveAndFlush(project);

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("PROJECT_NOT_OPEN"));

            assertThat(applicationRepository.findByProjectId(projectId)).isEmpty();
        }

        @Test
        @DisplayName("Expired projects reject applications")
        void submitApplication_afterDeadline_returns409WithStableCode() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            var project = projectRepository.findById(projectId).orElseThrow();
            project.setDeadline(LocalDateTime.now().minusMinutes(1));
            projectRepository.saveAndFlush(project);

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(
                            jsonPath("$.errorCode").value("PROJECT_APPLICATION_DEADLINE_PASSED"));

            assertThat(applicationRepository.findByProjectId(projectId)).isEmpty();
        }

        @Test
        @DisplayName("Full projects reject applications")
        void submitApplication_toFullProject_returns409WithStableCode() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            var project = projectRepository.findById(projectId).orElseThrow();
            project.setCurrentTeamSize(project.getMaxTeamSize());
            projectRepository.saveAndFlush(project);

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("PROJECT_FULL"));

            assertThat(applicationRepository.findByProjectId(projectId)).isEmpty();
        }

        @ParameterizedTest(name = "existing status {0}")
        @EnumSource(
                value = ApplicationStatus.class,
                names = {"WITHDRAWN", "REJECTED"})
        @DisplayName("Terminal applications do not permit reapplication")
        void submitApplication_afterTerminalApplication_returns409WithStableCode(
                ApplicationStatus terminalStatus) throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());
            var existing = applicationRepository.findByProjectId(projectId).getFirst();
            existing.setStatus(terminalStatus);
            applicationRepository.saveAndFlush(existing);

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("APPLICATION_ALREADY_EXISTS"));

            assertThat(applicationRepository.findByProjectId(projectId)).hasSize(1);
        }

        @Test
        @DisplayName("Concurrent duplicate submissions create exactly one application")
        void submitApplication_concurrently_isDeterministic() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            int attemptCount = 6;
            CountDownLatch ready = new CountDownLatch(attemptCount);
            CountDownLatch start = new CountDownLatch(1);
            List<Future<MvcResult>> requests = new ArrayList<>();

            for (int attempt = 0; attempt < attemptCount; attempt++) {
                requests.add(
                        requestExecutor.submit(
                                () -> {
                                    ready.countDown();
                                    start.await();
                                    return mockMvc.perform(
                                                    post(submitApplicationUrl(projectId))
                                                            .header(
                                                                    "Authorization",
                                                                    "Bearer " + applicantToken)
                                                            .contentType(
                                                                    MediaType.APPLICATION_JSON))
                                            .andReturn();
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
                    .containsExactlyInAnyOrder(201, 409, 409, 409, 409, 409);
            for (MvcResult result : results) {
                if (result.getResponse().getStatus() == 409) {
                    assertThat(
                                    objectMapper
                                            .readTree(result.getResponse().getContentAsString())
                                            .get("errorCode")
                                            .asText())
                            .isEqualTo("APPLICATION_ALREADY_EXISTS");
                }
            }
            assertThat(applicationRepository.findByProjectId(projectId)).hasSize(1);
        }

        @Test
        @DisplayName("4. Submit application without authentication returns 403 FORBIDDEN")
        void submitApplication_noAuth_returns403() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("5. Submit application to non-existent project returns 404 NOT_FOUND")
        void submitApplication_nonExistentProject_returns404() throws Exception {
            // Create an applicant (need a project owner first for infrastructure, but use applicant
            // token)
            createProjectOwnerAndGetToken();
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl("01NONEXISTENT0000000000000"))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("6. Multiple users can apply to the same project")
        void submitApplication_multipleApplicants_allPersisted() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            String applicant1Token = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicant2Token = createApplicantAndGetToken(APPLICANT2_EMAIL, "Ayse");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicant1Token)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicant2Token)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            var applications = applicationRepository.findByProjectId(projectId);
            assertThat(applications).hasSize(2);
        }

        @Test
        @DisplayName("7. Same user can apply to different projects")
        void submitApplication_differentProjects_allPersisted() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId1 = createProjectAndGetId(ownerToken);
            String projectId2 = createProjectAndGetId(ownerToken);

            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId1))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post(submitApplicationUrl(projectId2))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            String applicantId = getUserId(APPLICANT_EMAIL);
            var applications = applicationRepository.findByUserId(applicantId);
            assertThat(applications).hasSize(2);
        }
    }

    // ── 2. Review Application Tests ─────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/applications/{applicationId}/review")
    class ReviewApplicationTests {

        private String createApplicationAndGetId(String projectId, String applicantToken)
                throws Exception {
            MvcResult result =
                    mockMvc.perform(
                                    post(submitApplicationUrl(projectId))
                                            .header("Authorization", "Bearer " + applicantToken)
                                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(status().isCreated())
                            .andReturn();

            var jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            return jsonNode.get("data").get("applicationId").asText();
        }

        @Test
        @DisplayName("1. Approve application returns 200 OK")
        void reviewApplication_approve_returns200() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);

            String reviewBody =
                    """
                    {
                        "status": "APPROVED",
                        "reviewMessage": "Welcome to the team!"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                    .andExpect(jsonPath("$.data.projectId").value(projectId))
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));
        }

        @Test
        @DisplayName("2. Reject application returns 200 OK")
        void reviewApplication_reject_returns200() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);

            String reviewBody =
                    """
                    {
                        "status": "REJECTED",
                        "reviewMessage": "Unfortunately we need different skills."
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.applicationId").value(applicationId))
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));
        }

        @Test
        @DisplayName("Applicant cannot review their own application")
        void reviewApplication_asApplicant_returns403AndDoesNotMutate() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);
            int teamSizeBeforeReview =
                    projectRepository.findById(projectId).orElseThrow().getCurrentTeamSize();

            String reviewBody =
                    """
                    {
                        "status": "APPROVED",
                        "reviewMessage": "self-approved",
                        "requesterId": "forged-owner-id",
                        "requesterRole": "ADMIN"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isForbidden());

            var savedApplication = applicationRepository.findById(applicationId).orElseThrow();
            assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(projectRepository.findById(projectId).orElseThrow().getCurrentTeamSize())
                    .isEqualTo(teamSizeBeforeReview);
        }

        @Test
        @DisplayName("Unrelated authenticated user cannot review an application")
        void reviewApplication_asUnrelatedUser_returns403AndDoesNotMutate() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String unrelatedToken = createApplicantAndGetToken(APPLICANT2_EMAIL, "Ayse");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);
            int teamSizeBeforeReview =
                    projectRepository.findById(projectId).orElseThrow().getCurrentTeamSize();

            String reviewBody =
                    """
                    {
                        "status": "REJECTED",
                        "reviewMessage": "unauthorized rejection"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + unrelatedToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isForbidden());

            var savedApplication = applicationRepository.findById(applicationId).orElseThrow();
            assertThat(savedApplication.getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(projectRepository.findById(projectId).orElseThrow().getCurrentTeamSize())
                    .isEqualTo(teamSizeBeforeReview);
        }

        @Test
        @DisplayName("3. Approved application status is persisted in database")
        void reviewApplication_approve_persistedInDatabase() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);

            String reviewBody =
                    """
                    {
                        "status": "APPROVED"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isOk());

            var savedApp = applicationRepository.findById(applicationId).orElseThrow();
            assertThat(savedApp.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
        }

        @Test
        @DisplayName("4. Review non-existent application returns 404 NOT_FOUND")
        void reviewApplication_nonExistent_returns404() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();

            String reviewBody =
                    """
                    {
                        "status": "APPROVED"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl("01NONEXISTENT0000000000000"))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("5. Review without authentication returns 403 FORBIDDEN")
        void reviewApplication_noAuth_returns403() throws Exception {
            String reviewBody =
                    """
                    {
                        "status": "APPROVED"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl("01SOMEAPPLICATIONID00000000"))
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("6. Review with PENDING status returns 400 BAD_REQUEST")
        void reviewApplication_pendingStatus_returns400() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);

            String reviewBody =
                    """
                    {
                        "status": "PENDING"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("7. Review without status returns 400 BAD_REQUEST")
        void reviewApplication_noStatus_returns400() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);

            String reviewBody =
                    """
                    {
                        "reviewMessage": "Some message"
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("8. Approval review message is persisted and queryable")
        void reviewApplication_approvalMessage_isPersistedAndQueryable() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);
            String reviewMessage = "Great portfolio! Welcome aboard.";

            String reviewBody =
                    """
                    {
                        "status": "APPROVED",
                        "reviewMessage": "Great portfolio! Welcome aboard."
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("APPROVED"));

            var savedApp = applicationRepository.findById(applicationId).orElseThrow();
            assertThat(savedApp.getReviewMessage()).isEqualTo(reviewMessage);

            mockMvc.perform(
                            get(MY_APPLICATIONS_URL)
                                    .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.applications.length()").value(1))
                    .andExpect(
                            jsonPath("$.data.applications[0].applicationId").value(applicationId))
                    .andExpect(jsonPath("$.data.applications[0].status").value("APPROVED"))
                    .andExpect(
                            jsonPath("$.data.applications[0].reviewMessage").value(reviewMessage));
        }

        @Test
        @DisplayName("9. Rejection review message is persisted and queryable")
        void reviewApplication_rejectionMessage_isPersistedAndQueryable() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);
            String reviewMessage = "Unfortunately we need different skills.";

            String reviewBody =
                    """
                    {
                        "status": "REJECTED",
                        "reviewMessage": "Unfortunately we need different skills."
                    }
                    """;

            mockMvc.perform(
                            put(reviewApplicationUrl(applicationId))
                                    .header("Authorization", "Bearer " + ownerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(reviewBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("REJECTED"));

            var savedApp = applicationRepository.findById(applicationId).orElseThrow();
            assertThat(savedApp.getReviewMessage()).isEqualTo(reviewMessage);

            mockMvc.perform(
                            get(projectApplicationsUrl(projectId))
                                    .header("Authorization", "Bearer " + ownerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].applicationId").value(applicationId))
                    .andExpect(jsonPath("$.data[0].status").value("REJECTED"))
                    .andExpect(jsonPath("$.data[0].reviewMessage").value(reviewMessage));
        }
    }

    // ── 3. Get Project Applications Tests ───────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/projects/{projectId}/applications")
    class GetProjectApplicationsTests {

        @Test
        @DisplayName("1. Owner can list applications for their project")
        void getProjectApplications_asOwner_returns200() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            get(projectApplicationsUrl(projectId))
                                    .header("Authorization", "Bearer " + ownerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].applicationId").isNotEmpty())
                    .andExpect(jsonPath("$.data[0].applicantEmail").value(APPLICANT_EMAIL))
                    .andExpect(jsonPath("$.data[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("2. Owner sees all applicants when multiple users applied")
        void getProjectApplications_multipleApplicants_returnsAll() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            String applicant1Token = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicant2Token = createApplicantAndGetToken(APPLICANT2_EMAIL, "Ayse");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicant1Token)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicant2Token)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            get(projectApplicationsUrl(projectId))
                                    .header("Authorization", "Bearer " + ownerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("3. Non-owner cannot list applications returns 403 FORBIDDEN")
        void getProjectApplications_asNonOwner_returns403() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            get(projectApplicationsUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("4. Unauthenticated request returns 403 FORBIDDEN")
        void getProjectApplications_noAuth_returns403() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            mockMvc.perform(get(projectApplicationsUrl(projectId)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("5. Non-existent project returns 404 NOT_FOUND")
        void getProjectApplications_nonExistentProject_returns404() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            get(projectApplicationsUrl("01NONEXISTENT0000000000000"))
                                    .header("Authorization", "Bearer " + ownerToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("6. Empty list returned when project has no applications")
        void getProjectApplications_noApplications_returnsEmptyList() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            mockMvc.perform(
                            get(projectApplicationsUrl(projectId))
                                    .header("Authorization", "Bearer " + ownerToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    // ── 4. Get My Applications Tests ────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/users/me/applications")
    class GetMyApplicationsTests {

        @Test
        @DisplayName("1. User can list their own submitted applications")
        void getMyApplications_returns200WithList() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            get(MY_APPLICATIONS_URL)
                                    .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.applications").isArray())
                    .andExpect(jsonPath("$.data.applications.length()").value(1))
                    .andExpect(jsonPath("$.data.applications[0].projectId").value(projectId))
                    .andExpect(
                            jsonPath("$.data.applications[0].projectTitle")
                                    .value("AI Chatbot Project"))
                    .andExpect(jsonPath("$.data.applications[0].status").value("PENDING"));
        }

        @Test
        @DisplayName("2. Empty list returned when user has no applications")
        void getMyApplications_noApplications_returnsEmptyList() throws Exception {
            createProjectOwnerAndGetToken();
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            get(MY_APPLICATIONS_URL)
                                    .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.applications").isArray())
                    .andExpect(jsonPath("$.data.applications").isEmpty());
        }

        @Test
        @DisplayName("3. User sees applications to multiple projects")
        void getMyApplications_multipleProjects_returnsAll() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId1 = createProjectAndGetId(ownerToken);
            String projectId2 = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");

            mockMvc.perform(
                            post(submitApplicationUrl(projectId1))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post(submitApplicationUrl(projectId2))
                                    .header("Authorization", "Bearer " + applicantToken)
                                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            get(MY_APPLICATIONS_URL)
                                    .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.applications.length()").value(2));
        }

        @Test
        @DisplayName("4. Unauthenticated request returns 403 FORBIDDEN")
        void getMyApplications_noAuth_returns403() throws Exception {
            mockMvc.perform(get(MY_APPLICATIONS_URL)).andExpect(status().isForbidden());
        }
    }
}
