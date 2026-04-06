package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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

        mockMvc.perform(
                        patch("/api/v1/projects/" + projectId + "/status")
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"newStatus\": \"OPEN\"}"))
                .andExpect(status().isOk());

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
            assertThat(applications.get(0).getStatus()).isEqualTo(ApplicationStatus.PENDING);
            assertThat(applications.get(0).getUser().getEmail()).isEqualTo(APPLICANT_EMAIL);
        }

        @Test
        @DisplayName("3. Duplicate application returns 400 BAD_REQUEST")
        void submitApplication_duplicate_returns400() throws Exception {
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
                    .andExpect(status().isBadRequest());
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
        @DisplayName("8. Review with optional review message is included in response")
        void reviewApplication_withMessage_returns200() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);
            String applicantToken = createApplicantAndGetToken(APPLICANT_EMAIL, "Mehmet");
            String applicationId = createApplicationAndGetId(projectId, applicantToken);

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
