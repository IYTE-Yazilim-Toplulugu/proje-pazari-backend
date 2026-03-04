package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.domain.enums.RoleType;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import com.iyte_yazilim.proje_pazari.presentation.security.JwtUtil;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

class ProjectControllerIntegrationTest extends IntegrationTestBase {

    private static final String BASE_URL = "/api/v1/projects";
    private static final String VALID_EMAIL = "projectowner@std.iyte.edu.tr";
    private static final String VALID_PASSWORD = "SecurePass123!";
    private static final String VALID_FIRST_NAME = "Ali";
    private static final String VALID_LAST_NAME = "Yilmaz";

    private static final String APPLICANT_EMAIL = "applicant@std.iyte.edu.tr";

    @Autowired private UserRepository userRepository;

    @Autowired private EmailVerificationRepository emailVerificationRepository;

    @Autowired private ProjectRepository projectRepository;

    @Autowired private JwtUtil jwtUtil;

    // ── Helper Methods ──────────────────────────────────────────────────

    private void registerUser(String email, String password, String firstName, String lastName)
            throws Exception {
        var command = new RegisterUserCommand(email, password, firstName, lastName);
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());
    }

    private void registerAndVerifyUser(
            String email, String password, String firstName, String lastName) throws Exception {
        registerUser(email, password, firstName, lastName);

        var user = userRepository.findByEmail(email).orElseThrow();
        var verification =
                emailVerificationRepository
                        .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        var command = new LoginUserCommand(email, password);
        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(command)))
                        .andExpect(status().isOk())
                        .andReturn();

        var jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("data").get("accessToken").asText();
    }

    private void promoteToProjectOwner(String email) {
        UserEntity user = userRepository.findByEmail(email).orElseThrow();
        user.setRole(RoleType.PROJECT_OWNER);
        userRepository.save(user);
    }

    private String createProjectOwnerAndGetToken() throws Exception {
        registerAndVerifyUser(VALID_EMAIL, VALID_PASSWORD, VALID_FIRST_NAME, VALID_LAST_NAME);
        promoteToProjectOwner(VALID_EMAIL);
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
        @DisplayName("3. Create project with APPLICANT role returns 403 FORBIDDEN")
        void createProject_asApplicant_returns403() throws Exception {
            String token = createApplicantAndGetToken();

            mockMvc.perform(
                            post(BASE_URL)
                                    .header("Authorization", "Bearer " + token)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(validProjectData())))
                    .andExpect(status().isForbidden());
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
        void createProject_withTagsAndSkills_returns201() throws Exception {
            String token = createProjectOwnerAndGetToken();

            Map<String, Object> data = validProjectData();
            data.put("tags", new String[] {"ai", "nlp", "chatbot"});
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
}
