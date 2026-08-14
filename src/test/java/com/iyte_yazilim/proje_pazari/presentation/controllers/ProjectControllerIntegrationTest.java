package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

class ProjectControllerIntegrationTest extends IntegrationTestBase {

    private static final String BASE_URL = "/api/v1/projects";
    private static final String VALID_EMAIL = "projectowner@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "Ali";
    private static final String VALID_LAST_NAME = "Yilmaz";

    private static final String APPLICANT_EMAIL = "applicant@std.iyte.edu.tr";

    @Autowired private ProjectRepository projectRepository;

    @Autowired private JwtUtil jwtUtil;

    @Autowired private PasswordEncoder passwordEncoder;

    private String testUserId;
    private String jwtToken;

    // ── Helper Methods ──────────────────────────────────────────────────

    @BeforeEach
    void createDefaultUser() {
        UserEntity user = new UserEntity();
        user.setEmail("projcontroller-" + System.nanoTime() + "@std.iyte.edu.tr");
        user.setPassword(passwordEncoder.encode("TestPassword123!"));
        user.setFirstName("Project");
        user.setLastName("Owner");
        user.setIsActive(true);
        UserEntity saved = userRepository.save(user);

        EmailVerificationEntity verification = new EmailVerificationEntity();
        verification.setUserId(saved.getId());
        verification.setEmail(saved.getEmail());
        verification.setToken("dummy-token-" + System.nanoTime());
        verification.setExpiresAt(LocalDateTime.now().plusHours(24));
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        testUserId = saved.getId();
        jwtToken = jwtUtil.generateToken(saved.getId(), saved.getEmail(), "USER");
    }

    private String createProjectOwnerAndGetToken() throws Exception {
        registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        return loginAndGetToken(VALID_EMAIL, VALID_PASSWORD);
    }

    private String createApplicantAndGetToken() throws Exception {
        registerAndVerifyUser(APPLICANT_EMAIL, VALID_PASSWORD, "Mehmet", "Demir");
        return loginAndGetToken(APPLICANT_EMAIL, VALID_PASSWORD);
    }

    private String getUserId(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
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

    // ── 1. Create Project Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("POST /api/v1/projects")
    class CreateProjectTests {

        @Test
        @DisplayName("1. Create project with PROJECT_OWNER role returns 201 CREATED")
        void createProject_asProjectOwner_returns201() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validProjectData())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.projectId").isNotEmpty())
                    .andExpect(jsonPath("$.data.projectName").value("AI Chatbot Project"))
                    .andExpect(jsonPath("$.data.description").isNotEmpty())
                    .andExpect(jsonPath("$.data.ownerId").isNotEmpty())
                    .andExpect(jsonPath("$.data.maxTeamSize").value(5))
                    .andExpect(jsonPath("$.data.currentTeamSize").value(1));
        }

        @Test
        @DisplayName("2. Create project without authentication returns 403 FORBIDDEN")
        void createProject_noAuth_returns403() throws Exception {
            mockMvc.perform(
                            post(BASE_URL)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validProjectData())))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("3. Create project with any authenticated user returns 201 CREATED")
        void createProject_asAuthenticatedUser_returns201() throws Exception {
            String token = createApplicantAndGetToken();

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validProjectData())))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("4. Create project with missing project name returns 400 BAD_REQUEST")
        void createProject_missingName_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = validProjectData();
            data.remove("projectName");

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("5. Create project with empty body returns 400 BAD_REQUEST")
        void createProject_emptyBody_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("6. Create project with short project name returns 400 BAD_REQUEST")
        void createProject_shortName_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = validProjectData();
            data.put("projectName", "AB");

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("7. Create project with short description returns 400 BAD_REQUEST")
        void createProject_shortDescription_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = validProjectData();
            data.put("description", "Short");

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("8. Create project with past deadline returns 400 BAD_REQUEST")
        void createProject_pastDeadline_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            String json =
                    """
                    {
                        "projectName": "AI Chatbot Project",
                        "description": "Building an AI-powered chatbot for customer support using modern NLP techniques.",
                        "summary": "AI-powered chatbot for customer support using modern NLP techniques",
                        "maxTeamSize": 5,
                        "deadline": "%s"
                    }
                    """
                            .formatted(LocalDateTime.now().minusDays(1).toString());

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("9. Create project with zero maxTeamSize returns 400 BAD_REQUEST")
        void createProject_zeroMaxTeamSize_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = validProjectData();
            data.put("maxTeamSize", 0);

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("10. Created project is persisted in database")
        void createProject_persistedInDatabase() throws Exception {
            String token = createProjectOwnerAndGetToken();

            MvcResult result =
                    mockMvc.perform(
                                    post(BASE_URL)
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            validProjectData())))
                            .andExpect(status().isCreated())
                            .andReturn();

            var jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
            String projectId = jsonNode.get("data").get("projectId").asText();

            var savedProject = projectRepository.findById(projectId);
            assertThat(savedProject).isPresent();
            assertThat(savedProject.get().getTitle()).isEqualTo("AI Chatbot Project");
            assertThat(savedProject.get().getCurrentTeamSize()).isEqualTo(1);
        }

        @Test
        @DisplayName("11. Created project owner ID matches authenticated user")
        void createProject_ownerIdMatchesAuthUser() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String expectedOwnerId = getUserId(VALID_EMAIL);

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validProjectData())))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.ownerId").value(expectedOwnerId));
        }

        @Test
        @DisplayName("12. Create project with valid future deadline returns 201 CREATED")
        void createProject_futureDeadline_returns201() throws Exception {
            String token = createProjectOwnerAndGetToken();

            String json =
                    """
                    {
                        "projectName": "AI Chatbot Project",
                        "description": "Building an AI-powered chatbot for customer support using modern NLP techniques.",
                        "summary": "AI-powered chatbot for customer support using modern NLP techniques",
                        "maxTeamSize": 5,
                        "deadline": "%s"
                    }
                    """
                            .formatted(LocalDateTime.now().plusMonths(3).toString());

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(json))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.projectId").isNotEmpty());
        }

        @Test
        @DisplayName("13. Create project with tags and required skills returns 201 CREATED")
        void createProject_withRequiredSkills_returns201() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = validProjectData();
            data.put("requiredSkills", new String[] {"Java", "Spring Boot", "Docker"});

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.requiredSkills").isArray());
        }

        @Test
        @DisplayName("14. Create project with only required fields returns 201 CREATED")
        void createProject_onlyRequiredFields_returns201() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = new HashMap<>();
            data.put("projectName", "Minimal Project");
            data.put(
                    "description",
                    "A minimal project with only required fields for testing purposes.");
            data.put("summary", "A minimal project for testing required fields only");

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.projectId").isNotEmpty())
                    .andExpect(jsonPath("$.data.projectName").value("Minimal Project"));
        }

        @Test
        @DisplayName("15. Multiple projects can be created by the same owner")
        void createProject_multipleProjects_allPersisted() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String ownerId = getUserId(VALID_EMAIL);

            Map<String, Object> data1 = validProjectData();
            data1.put("projectName", "Project Alpha");

            Map<String, Object> data2 = validProjectData();
            data2.put("projectName", "Project Beta");

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data1)))
                    .andExpect(status().isCreated());

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(data2)))
                    .andExpect(status().isCreated());

            var projects = projectRepository.findByOwnerId(ownerId);
            assertThat(projects).hasSize(2);
        }
    }

    @Test
    @DisplayName("16. Create project response does not contain teamMemberIds or tags")
    void createProject_response_doesNotContainRemovedFields() throws Exception {
        String token = createProjectOwnerAndGetToken();

        mockMvc.perform(
                        post(BASE_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(validProjectData())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teamMemberIds").doesNotExist())
                .andExpect(jsonPath("$.data.tags").doesNotExist());
    }

    @Test
    @DisplayName(
            "17. Create project request with legacy tags and teamMemberIds fields is ignored and returns 201")
    void createProject_withLegacyFields_ignoredAndReturns201() throws Exception {
        String token = createProjectOwnerAndGetToken();

        Map<String, Object> data = validProjectData();
        data.put("tags", new String[] {"ai", "nlp"});
        data.put("teamMemberIds", new String[] {"01HQZX...", "01HQZY..."});

        mockMvc.perform(
                        post(BASE_URL)
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(data)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.teamMemberIds").doesNotExist())
                .andExpect(jsonPath("$.data.tags").doesNotExist());
    }

    // ── 2. Get All Projects Tests ───────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/projects")
    class GetAllProjectsTests {

        @Test
        @DisplayName("1. Get all projects returns 200 with empty list when no projects exist")
        void getAllProjects_noProjects_returns200EmptyList() throws Exception {
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray());
        }

        @Test
        @DisplayName("2. Get all projects returns 200 with created projects")
        void getAllProjects_withProjects_returns200WithList() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validProjectData())))
                    .andExpect(status().isCreated());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects").isArray())
                    .andExpect(jsonPath("$.data.projects.length()").value(1));
        }

        @Test
        @DisplayName("3. Get all projects is accessible without authentication")
        void getAllProjects_noAuth_returns200() throws Exception {
            mockMvc.perform(get(BASE_URL)).andExpect(status().isOk());
        }

        @ParameterizedTest(name = "4. Get all projects filters by status {0}")
        @EnumSource(ProjectStatus.class)
        void getAllProjects_withStatus_returnsOnlyMatchingProjects(ProjectStatus status)
                throws Exception {
            UserEntity owner = userRepository.findById(testUserId).orElseThrow();
            String matchingTitle = status.name() + " Project";
            projectRepository.save(buildProject(matchingTitle, status, owner));
            projectRepository.save(buildProject("Other Project", differentStatus(status), owner));

            mockMvc.perform(get(BASE_URL).param("status", status.name()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projects.length()").value(1))
                    .andExpect(jsonPath("$.data.projects[0].projectName").value(matchingTitle))
                    .andExpect(jsonPath("$.data.projects[0].status").value(status.name()))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }

        @Test
        @DisplayName("5. Invalid status filter returns 400 BAD_REQUEST")
        void getAllProjects_invalidStatus_returns400() throws Exception {
            mockMvc.perform(get(BASE_URL).param("status", "INVALID_VALUE"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4));
        }

        private ProjectStatus differentStatus(ProjectStatus status) {
            return status == ProjectStatus.OPEN ? ProjectStatus.DRAFT : ProjectStatus.OPEN;
        }

        private ProjectEntity buildProject(String title, ProjectStatus status, UserEntity owner) {
            ProjectEntity project = new ProjectEntity();
            project.setTitle(title);
            project.setDescription("Project used for status filtering integration coverage.");
            project.setSummary(title);
            project.setStatus(status);
            project.setOwner(owner);
            project.setApplications(List.of());
            project.setMaxTeamSize(5);
            project.setRequiredSkills(List.of("Java", "Spring Boot"));
            project.setCategory("Backend");
            return project;
        }
    }

    // ── 3. Update Project Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("PUT /api/v1/projects/{projectId}")
    class UpdateProjectTests {

        private String createProjectAndGetId(String token) throws Exception {
            MvcResult result =
                    mockMvc.perform(
                                    post(BASE_URL)
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            validProjectData())))
                            .andExpect(status().isCreated())
                            .andReturn();
            return objectMapper
                    .readTree(result.getResponse().getContentAsString())
                    .get("data")
                    .get("projectId")
                    .asText();
        }

        @Test
        @DisplayName("1. Owner can update project name")
        void updateProject_asOwner_returns200() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            Map<String, Object> update = new HashMap<>();
            update.put("projectName", "Updated AI Project Name");
            update.put("summary", "Updated AI-powered chatbot for customer support");

            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(update)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projectName").value("Updated AI Project Name"));
        }

        @Test
        @DisplayName("2. Update without authentication returns 403 FORBIDDEN")
        void updateProject_noAuth_returns403() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"projectName\": \"Some Name\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("3. Non-owner cannot update project returns 403 FORBIDDEN")
        void updateProject_asNonOwner_returns403() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            // Create a second project owner
            registerAndVerifyUser("owner2@std.iyte.edu.tr", VALID_PASSWORD, "Zeynep", "Sahin");
            String otherOwnerToken = loginAndGetToken("owner2@std.iyte.edu.tr", VALID_PASSWORD);

            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + otherOwnerToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"projectName\": \"Hijacked Name\", \"summary\": \"Hijacked project summary\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("4. Update non-existent project returns 404 NOT_FOUND")
        void updateProject_nonExistent_returns404() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            put(BASE_URL + "/01NONEXISTENT0000000000000")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"projectName\": \"Some Name\", \"summary\": \"Summary for non-existent project test\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("5. Update with too-short name returns 400 BAD_REQUEST")
        void updateProject_shortName_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"projectName\": \"AB\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("6. Updated fields are persisted in database")
        void updateProject_persistedInDatabase() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"projectName\": \"Persisted Name\", \"summary\": \"Persisted project summary for database test\"}"))
                    .andExpect(status().isOk());

            var saved = projectRepository.findById(projectId).orElseThrow();
            assertThat(saved.getTitle()).isEqualTo("Persisted Name");
        }

        @Test
        @DisplayName("7. Update only projectName without summary preserves existing summary")
        void updateProject_withoutSummary_preservesExistingSummary() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            // Only send projectName — no summary field
            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"projectName\": \"Updated Name Only\"}"))
                    .andExpect(status().isOk());

            var saved = projectRepository.findById(projectId).orElseThrow();
            assertThat(saved.getTitle()).isEqualTo("Updated Name Only");
            assertThat(saved.getSummary())
                    .isEqualTo(
                            "AI-powered chatbot for customer support using modern NLP techniques");
        }

        @Test
        @DisplayName("8. Update summary persists it and GET returns new value")
        void updateProject_summary_persistedAndReturnedByGet() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            put(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(
                                            "{\"projectName\": \"AI Chatbot Project\","
                                                    + " \"summary\": \"Updated summary value\"}"))
                    .andExpect(status().isOk());

            mockMvc.perform(get(BASE_URL + "/" + projectId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.summary").value("Updated summary value"));
        }
    }

    // ── 4. Delete Project Tests ─────────────────────────────────────────

    @Nested
    @DisplayName("DELETE /api/v1/projects/{projectId}")
    class DeleteProjectTests {

        private String createProjectAndGetId(String token) throws Exception {
            MvcResult result =
                    mockMvc.perform(
                                    post(BASE_URL)
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            validProjectData())))
                            .andExpect(status().isCreated())
                            .andReturn();
            return objectMapper
                    .readTree(result.getResponse().getContentAsString())
                    .get("data")
                    .get("projectId")
                    .asText();
        }

        @Test
        @DisplayName("1. Owner can delete their project returns 200 OK")
        void deleteProject_asOwner_returns200() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            delete(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            assertThat(projectRepository.findById(projectId)).isEmpty();
        }

        @Test
        @DisplayName("2. Delete without authentication returns 403 FORBIDDEN")
        void deleteProject_noAuth_returns403() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(delete(BASE_URL + "/" + projectId)).andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("3. Non-owner cannot delete project returns 403 FORBIDDEN")
        void deleteProject_asNonOwner_returns403() throws Exception {
            String ownerToken = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(ownerToken);

            registerAndVerifyUser("owner2@std.iyte.edu.tr", VALID_PASSWORD, "Zeynep", "Sahin");
            String otherOwnerToken = loginAndGetToken("owner2@std.iyte.edu.tr", VALID_PASSWORD);

            mockMvc.perform(
                            delete(BASE_URL + "/" + projectId)
                                    .header("Authorization", "Bearer " + otherOwnerToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("4. Delete non-existent project returns 404 NOT_FOUND")
        void deleteProject_nonExistent_returns404() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            delete(BASE_URL + "/01NONEXISTENT0000000000000")
                                    .header("Authorization", "Bearer " + token))
                    .andExpect(status().isNotFound());
        }
    }

    // ── 5. Update Project Status Tests ──────────────────────────────────

    @Nested
    @DisplayName("PATCH /api/v1/projects/{projectId}/status")
    class UpdateProjectStatusTests {

        private String createProjectAndGetId(String token) throws Exception {
            MvcResult result =
                    mockMvc.perform(
                                    post(BASE_URL)
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            validProjectData())))
                            .andExpect(status().isCreated())
                            .andReturn();
            return objectMapper
                    .readTree(result.getResponse().getContentAsString())
                    .get("data")
                    .get("projectId")
                    .asText();
        }

        @Test
        @DisplayName("1. Owner can change project status to OPEN")
        void updateStatus_toActive_returns200() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            patch(BASE_URL + "/" + projectId + "/status")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"newStatus\": \"OPEN\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.newStatus").value("OPEN"))
                    .andExpect(jsonPath("$.data.oldStatus").value("DRAFT"));
        }

        @Test
        @DisplayName("2. Status change is persisted in database")
        void updateStatus_persistedInDatabase() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            patch(BASE_URL + "/" + projectId + "/status")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"newStatus\": \"OPEN\"}"))
                    .andExpect(status().isOk());

            var saved = projectRepository.findById(projectId).orElseThrow();
            assertThat(saved.getStatus().toString()).isEqualTo("OPEN");
        }

        @Test
        @DisplayName("3. Same status returns 400 BAD_REQUEST")
        void updateStatus_sameStatus_returns400() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            // Project starts as DRAFT — setting DRAFT again is a no-op
            mockMvc.perform(
                            patch(BASE_URL + "/" + projectId + "/status")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"newStatus\": \"DRAFT\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("4. Update status without authentication returns 403 FORBIDDEN")
        void updateStatus_noAuth_returns403() throws Exception {
            String token = createProjectOwnerAndGetToken();
            String projectId = createProjectAndGetId(token);

            mockMvc.perform(
                            patch(BASE_URL + "/" + projectId + "/status")
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"newStatus\": \"OPEN\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("5. Update status on non-existent project returns 404 NOT_FOUND")
        void updateStatus_nonExistent_returns404() throws Exception {
            String token = createProjectOwnerAndGetToken();

            mockMvc.perform(
                            patch(BASE_URL + "/01NONEXISTENT0000000000000/status")
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content("{\"newStatus\": \"OPEN\"}"))
                    .andExpect(status().isNotFound());
        }
    }

    // ── 6. Get Project By ID Tests ──────────────────────────────────────

    @Nested
    @DisplayName("GET /api/v1/projects/{projectId}")
    class GetProjectByIdTests {

        @Test
        @DisplayName("1. Get existing project by ID returns 200")
        void getProjectById_existingProject_returns200() throws Exception {
            String token = createProjectOwnerAndGetToken();

            MvcResult result =
                    mockMvc.perform(
                                    post(BASE_URL)
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            validProjectData())))
                            .andExpect(status().isCreated())
                            .andReturn();

            String projectId =
                    objectMapper
                            .readTree(result.getResponse().getContentAsString())
                            .get("data")
                            .get("projectId")
                            .asText();

            mockMvc.perform(get(BASE_URL + "/" + projectId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.projectName").value("AI Chatbot Project"));
        }

        @Test
        @DisplayName("2. Get non-existent project by ID returns 404")
        void getProjectById_nonExistent_returns404() throws Exception {
            mockMvc.perform(get(BASE_URL + "/nonexistent-id")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("3. Get project by ID is accessible without authentication")
        void getProjectById_noAuth_returns404ForNonExistent() throws Exception {
            mockMvc.perform(get(BASE_URL + "/nonexistent-id")).andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("4. Created project summary is persisted and returned by GET")
        void getProjectById_returnsSummary() throws Exception {
            String token = createProjectOwnerAndGetToken();

            MvcResult result =
                    mockMvc.perform(
                                    post(BASE_URL)
                                            .header("Authorization", "Bearer " + token)
                                            .contentType(MediaType.APPLICATION_JSON)
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            validProjectData())))
                            .andExpect(status().isCreated())
                            .andReturn();

            String projectId =
                    objectMapper
                            .readTree(result.getResponse().getContentAsString())
                            .get("data")
                            .get("projectId")
                            .asText();

            mockMvc.perform(get(BASE_URL + "/" + projectId))
                    .andExpect(status().isOk())
                    .andExpect(
                            jsonPath("$.data.summary")
                                    .value(
                                            "AI-powered chatbot for customer support using modern NLP techniques"));
        }
    }
}
