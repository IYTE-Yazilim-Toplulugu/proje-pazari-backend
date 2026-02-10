package com.iyte_yazilim.proje_pazari.e2e;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserJourneyE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private static String jwtToken;
    private static String userId;

    @Test
    @Order(1)
    @DisplayName("E2E: User registers successfully")
    void step1_register() throws Exception {
        Map<String, String> request = Map.of(
                "email", "e2e-journey@std.iyte.edu.tr",
                "password", "SecureE2EPassword123!",
                "firstName", "E2E",
                "lastName", "Tester");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/auth/register", request, String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertNotNull(body.get("data"));
    }

    @Test
    @Order(2)
    @DisplayName("E2E: User logs in and receives JWT token")
    void step2_login() throws Exception {
        Map<String, String> request = Map.of(
                "email", "e2e-journey@std.iyte.edu.tr",
                "password", "SecureE2EPassword123!");

        ResponseEntity<String> response = restTemplate.postForEntity("/api/v1/auth/login", request, String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        jwtToken = body.get("data").get("token").asText();
        userId = body.get("data").get("userId").asText();

        assertNotNull(jwtToken);
        assertFalse(jwtToken.isEmpty());
        assertNotNull(userId);
    }

    @Test
    @Order(3)
    @DisplayName("E2E: User views own profile")
    void step3_viewProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/users/me", HttpMethod.GET, entity,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(userId, body.get("data").get("id").asText());
        assertEquals("E2E", body.get("data").get("firstName").asText());
        assertEquals("Tester", body.get("data").get("lastName").asText());
    }

    @Test
    @Order(4)
    @DisplayName("E2E: User updates profile")
    void step4_updateProfile() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> updateRequest = Map.of(
                "firstName", "Updated",
                "lastName", "E2ETester",
                "description", "An end-to-end test user profile");

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(updateRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/users/me", HttpMethod.PUT, entity,
                String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals("Updated", body.get("data").get("firstName").asText());
        assertEquals("E2ETester", body.get("data").get("lastName").asText());
    }

    @Test
    @Order(5)
    @DisplayName("E2E: User views public profile by ID")
    void step5_viewPublicProfile() throws Exception {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/users/{userId}", String.class, userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertEquals(userId, body.get("data").get("id").asText());
        // Should see updated name from step 4
        assertEquals("Updated", body.get("data").get("firstName").asText());
    }

    @Test
    @Order(6)
    @DisplayName("E2E: User creates a project")
    void step6_createProject() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> projectRequest = Map.of(
                "projectName", "E2E Test Project",
                "description",
                "A project created during the E2E user journey test to verify full flow",
                "ownerId", userId);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(projectRequest, headers);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/projects", HttpMethod.POST, entity,
                String.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        JsonNode body = objectMapper.readTree(response.getBody());
        assertNotNull(body.get("data"));
        assertEquals("E2E Test Project", body.get("data").get("title").asText());
    }

    @Test
    @Order(7)
    @DisplayName("E2E: Health check endpoint is accessible")
    void step7_healthCheck() {
        ResponseEntity<String> response = restTemplate.getForEntity("/api/v1/health", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
