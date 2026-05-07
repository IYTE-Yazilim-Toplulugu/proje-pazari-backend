package com.iyte_yazilim.proje_pazari.e2e;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.TestRateLimitConfig;
import com.iyte_yazilim.proje_pazari.TestRedisConfig;
import com.iyte_yazilim.proje_pazari.application.service.EmailService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.EmailVerificationEntity;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import({TestRedisConfig.class, TestRateLimitConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserJourneyE2ETest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private EmailVerificationRepository emailVerificationRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean private EmailService emailService;

    private static String jwtToken;
    private static String userId;
    private static String applicantToken;
    private static String projectId;

    @Test
    @Order(1)
    @DisplayName("E2E: User registers successfully")
    void step1_register() throws Exception {
        Map<String, String> request =
                Map.of(
                        "email", "e2e-journey@std.iyte.edu.tr",
                        "password", "SecureE2EPassword123!",
                        "firstName", "E2E",
                        "lastName", "Tester");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(2)
    @DisplayName("E2E: User verifies email, logs in and receives JWT token")
    void step2_login() throws Exception {
        // Verify user exists (all authenticated users can create projects now)
        com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity user =
                userRepository.findByEmail("e2e-journey@std.iyte.edu.tr").orElseThrow();

        EmailVerificationEntity verification =
                emailVerificationRepository
                        .findByEmailAndVerifiedAtIsNull("e2e-journey@std.iyte.edu.tr")
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        Map<String, String> request =
                Map.of(
                        "email", "e2e-journey@std.iyte.edu.tr",
                        "password", "SecureE2EPassword123!");

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.data.accessToken").exists())
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(responseBody);
        jwtToken = body.get("data").get("accessToken").asText();
        userId = body.get("data").get("userId").asText();
    }

    @Test
    @Order(3)
    @DisplayName("E2E: User views own profile")
    void step3_viewProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me").header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.firstName").value("E2E"))
                .andExpect(jsonPath("$.data.lastName").value("Tester"));
    }

    @Test
    @Order(4)
    @DisplayName("E2E: User updates profile")
    void step4_updateProfile() throws Exception {
        Map<String, String> updateRequest =
                Map.of(
                        "firstName", "Updated",
                        "lastName", "E2ETester",
                        "description", "An end-to-end test user profile");

        mockMvc.perform(
                        put("/api/v1/users/me")
                                .header("Authorization", "Bearer " + jwtToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.firstName").value("Updated"))
                .andExpect(jsonPath("$.data.lastName").value("E2ETester"));
    }

    @Test
    @Order(5)
    @DisplayName("E2E: User views public profile by ID")
    void step5_viewPublicProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.firstName").value("Updated"));
    }

    @Test
    @Order(6)
    @DisplayName("E2E: User creates a project")
    void step6_createProject() throws Exception {
        Map<String, Object> projectRequest =
                Map.of(
                        "projectName",
                        "E2E Test Project",
                        "description",
                        "A project created during the E2E user journey test to verify full flow",
                        "ownerId",
                        userId);

        MvcResult projectResult =
                mockMvc.perform(
                                post("/api/v1/projects")
                                        .header("Authorization", "Bearer " + jwtToken)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(projectRequest)))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.data.projectName").value("E2E Test Project"))
                        .andReturn();

        String projectResponse = projectResult.getResponse().getContentAsString();
        JsonNode projectBody = objectMapper.readTree(projectResponse);
        projectId = projectBody.get("data").get("projectId").asText();
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Health check endpoint is accessible")
    void step7_healthCheck() throws Exception {
        mockMvc.perform(get("/api/v1/health")).andExpect(status().isOk());
    }

    @Test
    @Order(8)
    @DisplayName("E2E: Second user registers and logs in as applicant")
    void step8_registerAndLoginApplicant() throws Exception {
        Map<String, String> registerRequest =
                Map.of(
                        "email", "e2e-applicant@std.iyte.edu.tr",
                        "password", "ApplicantPass123!",
                        "firstName", "Applicant",
                        "lastName", "User");

        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        EmailVerificationEntity verification =
                emailVerificationRepository
                        .findByEmailAndVerifiedAtIsNull("e2e-applicant@std.iyte.edu.tr")
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);

        Map<String, String> loginRequest =
                Map.of(
                        "email", "e2e-applicant@std.iyte.edu.tr",
                        "password", "ApplicantPass123!");

        MvcResult result =
                mockMvc.perform(
                                post("/api/v1/auth/login")
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        JsonNode body = objectMapper.readTree(responseBody);
        applicantToken = body.get("data").get("accessToken").asText();
    }

    @Test
    @Order(9)
    @DisplayName("E2E: Submitting an application triggers owner notification email")
    void step9_submitApplicationTriggersOwnerNotification() throws Exception {
        mockMvc.perform(
                        post("/api/v1/projects/{projectId}/applications", projectId)
                                .header("Authorization", "Bearer " + applicantToken)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        verify(emailService, timeout(5000))
                .sendTemplateEmailAsync(
                        eq("e2e-journey@std.iyte.edu.tr"),
                        eq("new-application-notification.html"),
                        anyMap());
    }
}
