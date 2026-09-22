package com.iyte_yazilim.proje_pazari.presentation.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MvcResult;

@ExtendWith(OutputCaptureExtension.class)
class ApplicationMessageControllerIntegrationTest extends IntegrationTestBase {

    private static final String OWNER_EMAIL = "message-owner@std.iyte.edu.tr";
    private static final String APPLICANT_EMAIL = "message-applicant@std.iyte.edu.tr";
    private static final String UNRELATED_EMAIL = "message-unrelated@std.iyte.edu.tr";
    private static final String PASSWORD = "SecurePass123!";

    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectApplicationRepository applicationRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void applicantCanSendAndListPlainTextMessages(CapturedOutput output) throws Exception {
        ThreadFixture fixture = createThread(false);
        String body = "<script>alert('still plain text')</script>";

        JsonNode sent = sendMessage(fixture.applicationId(), fixture.applicantToken(), body);

        assertThat(sent.path("body").asText()).isEqualTo(body);
        assertThat(sent.path("applicationId").asText()).isEqualTo(fixture.applicationId());
        assertThat(sent.path("senderId").asText()).isEqualTo(fixture.applicantId());
        assertThat(sent.path("createdAt").isTextual()).isTrue();

        mockMvc.perform(
                        get(messageUrl(fixture.applicationId()))
                                .header("Authorization", bearer(fixture.applicantToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].body").value(body))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        assertThat(output.getAll()).doesNotContain(body);
    }

    @Test
    void projectOwnerCanSendAndSeeTheSameThread() throws Exception {
        ThreadFixture fixture = createThread(false);
        sendMessage(fixture.applicationId(), fixture.applicantToken(), "Applicant hello");
        JsonNode ownerMessage =
                sendMessage(fixture.applicationId(), fixture.ownerToken(), "Owner reply");

        assertThat(ownerMessage.path("senderId").asText()).isEqualTo(fixture.ownerId());

        mockMvc.perform(
                        get(messageUrl(fixture.applicationId()))
                                .header("Authorization", bearer(fixture.ownerToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].body").value("Applicant hello"))
                .andExpect(jsonPath("$.data.content[1].body").value("Owner reply"));
    }

    @Test
    void unrelatedAuthenticatedUserCannotDiscoverListOrSendMessages() throws Exception {
        ThreadFixture fixture = createThread(true);

        mockMvc.perform(
                        get(messageUrl(fixture.applicationId()))
                                .header("Authorization", bearer(fixture.unrelatedToken())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"))
                .andExpect(jsonPath("$.data").doesNotExist());

        mockMvc.perform(
                        post(messageUrl(fixture.applicationId()))
                                .header("Authorization", bearer(fixture.unrelatedToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody("intrusion")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("ACCESS_DENIED"));

        assertThat(messageCount(fixture.applicationId())).isZero();
    }

    @Test
    void anonymousRequestsAreRejected() throws Exception {
        ThreadFixture fixture = createThread(false);

        mockMvc.perform(get(messageUrl(fixture.applicationId()))).andExpect(status().isForbidden());

        mockMvc.perform(
                        post(messageUrl(fixture.applicationId()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody("anonymous")))
                .andExpect(status().isForbidden());

        assertThat(messageCount(fixture.applicationId())).isZero();
    }

    @Test
    void blankAndOverLimitBodiesAreRejectedWithoutLeakingContentToLogs(CapturedOutput output)
            throws Exception {
        ThreadFixture fixture = createThread(false);

        mockMvc.perform(
                        post(messageUrl(fixture.applicationId()))
                                .header("Authorization", bearer(fixture.applicantToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody(" \n\t ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        String secretMarker = "MESSAGE_BODY_MUST_NOT_REACH_LOGS";
        mockMvc.perform(
                        post(messageUrl(fixture.applicationId()))
                                .header("Authorization", bearer(fixture.applicantToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody(secretMarker + "x".repeat(2_001))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));

        assertThat(messageCount(fixture.applicationId())).isZero();
        assertThat(output.getAll()).doesNotContain(secretMarker);
    }

    @Test
    void listingUsesBoundedPagination() throws Exception {
        ThreadFixture fixture = createThread(false);
        for (int index = 0; index < 12; index++) {
            sendMessage(fixture.applicationId(), fixture.applicantToken(), "message-" + index);
        }

        mockMvc.perform(
                        get(messageUrl(fixture.applicationId()))
                                .queryParam("page", "1")
                                .queryParam("size", "5")
                                .header("Authorization", bearer(fixture.applicantToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(5))
                .andExpect(jsonPath("$.data.content[0].body").value("message-5"))
                .andExpect(jsonPath("$.data.content[4].body").value("message-9"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(12))
                .andExpect(jsonPath("$.data.totalPages").value(3));

        mockMvc.perform(
                        get(messageUrl(fixture.applicationId()))
                                .queryParam("size", "101")
                                .header("Authorization", bearer(fixture.applicantToken())))
                .andExpect(status().isBadRequest());

        mockMvc.perform(
                        get(messageUrl(fixture.applicationId()))
                                .queryParam("page", "-1")
                                .header("Authorization", bearer(fixture.applicantToken())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void equalTimestampsAreOrderedByMessageId() throws Exception {
        ThreadFixture fixture = createThread(false);
        List<String> ids = new ArrayList<>();
        ids.add(
                sendMessage(fixture.applicationId(), fixture.applicantToken(), "first")
                        .path("id")
                        .asText());
        ids.add(
                sendMessage(fixture.applicationId(), fixture.ownerToken(), "second")
                        .path("id")
                        .asText());
        ids.add(
                sendMessage(fixture.applicationId(), fixture.applicantToken(), "third")
                        .path("id")
                        .asText());

        jdbcTemplate.update(
                "UPDATE application_messages SET created_at = ? WHERE application_id = ?",
                LocalDateTime.of(2026, 1, 1, 12, 0),
                fixture.applicationId());
        ids.sort(Comparator.naturalOrder());

        MvcResult result =
                mockMvc.perform(
                                get(messageUrl(fixture.applicationId()))
                                        .queryParam("size", "100")
                                        .header("Authorization", bearer(fixture.applicantToken())))
                        .andExpect(status().isOk())
                        .andReturn();

        JsonNode content =
                objectMapper
                        .readTree(result.getResponse().getContentAsString())
                        .path("data")
                        .path("content");
        assertThat(content)
                .extracting(node -> node.path("id").asText())
                .containsExactlyElementsOf(ids);
    }

    @Test
    void deletingApplicationCascadesToItsMessages() throws Exception {
        ThreadFixture fixture = createThread(false);
        sendMessage(fixture.applicationId(), fixture.ownerToken(), "temporary");
        assertThat(messageCount(fixture.applicationId())).isOne();

        applicationRepository.deleteById(fixture.applicationId());
        applicationRepository.flush();

        assertThat(messageCount(fixture.applicationId())).isZero();
    }

    @Test
    void missingApplicationReturnsNotFoundForListAndSend() throws Exception {
        ThreadFixture fixture = createThread(false);
        String missingId = "01NONEXISTENT0000000000000";

        mockMvc.perform(
                        get(messageUrl(missingId))
                                .header("Authorization", bearer(fixture.applicantToken())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("APPLICATION_NOT_FOUND"));

        mockMvc.perform(
                        post(messageUrl(missingId))
                                .header("Authorization", bearer(fixture.applicantToken()))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(jsonBody("hello")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("APPLICATION_NOT_FOUND"));
    }

    private ThreadFixture createThread(boolean includeUnrelatedUser) throws Exception {
        String ownerToken = createVerifiedUserAndGetToken(OWNER_EMAIL, PASSWORD, "Owner", "User");
        String applicantToken =
                createVerifiedUserAndGetToken(APPLICANT_EMAIL, PASSWORD, "Applicant", "User");
        String unrelatedToken =
                includeUnrelatedUser
                        ? createVerifiedUserAndGetToken(
                                UNRELATED_EMAIL, PASSWORD, "Unrelated", "User")
                        : null;

        MvcResult projectResult =
                mockMvc.perform(
                                post("/api/v1/projects")
                                        .header("Authorization", bearer(ownerToken))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(
                                                objectMapper.writeValueAsString(
                                                        Map.of(
                                                                "projectName",
                                                                "Messaging Project",
                                                                "description",
                                                                "A sufficiently detailed project description for messaging tests.",
                                                                "summary",
                                                                "Messaging integration test project",
                                                                "category",
                                                                "Testing",
                                                                "maxTeamSize",
                                                                4,
                                                                "requiredSkills",
                                                                List.of("Java")))))
                        .andExpect(status().isCreated())
                        .andReturn();
        String projectId =
                objectMapper
                        .readTree(projectResult.getResponse().getContentAsString())
                        .path("data")
                        .path("projectId")
                        .asText();
        var project = projectRepository.findById(projectId).orElseThrow();
        project.setStatus(ProjectStatus.OPEN);
        projectRepository.saveAndFlush(project);

        MvcResult applicationResult =
                mockMvc.perform(
                                post("/api/v1/projects/{projectId}/applications", projectId)
                                        .header("Authorization", bearer(applicantToken))
                                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isCreated())
                        .andReturn();
        String applicationId =
                objectMapper
                        .readTree(applicationResult.getResponse().getContentAsString())
                        .path("data")
                        .path("applicationId")
                        .asText();

        return new ThreadFixture(
                applicationId,
                ownerToken,
                applicantToken,
                unrelatedToken,
                userId(OWNER_EMAIL),
                userId(APPLICANT_EMAIL));
    }

    private JsonNode sendMessage(String applicationId, String token, String body) throws Exception {
        MvcResult result =
                mockMvc.perform(
                                post(messageUrl(applicationId))
                                        .header("Authorization", bearer(token))
                                        .contentType(MediaType.APPLICATION_JSON)
                                        .content(jsonBody(body)))
                        .andExpect(status().isCreated())
                        .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("data");
    }

    private String jsonBody(String body) throws Exception {
        return objectMapper.writeValueAsString(Map.of("body", body));
    }

    private String messageUrl(String applicationId) {
        return "/api/v1/applications/" + applicationId + "/messages";
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String userId(String email) {
        return userRepository.findByEmail(email).orElseThrow().getId();
    }

    private int messageCount(String applicationId) {
        Integer count =
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM application_messages WHERE application_id = ?",
                        Integer.class,
                        applicationId);
        return count == null ? 0 : count;
    }

    private record ThreadFixture(
            String applicationId,
            String ownerToken,
            String applicantToken,
            String unrelatedToken,
            String ownerId,
            String applicantId) {}
}
