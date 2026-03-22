package com.iyte_yazilim.proje_pazari;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iyte_yazilim.proje_pazari.application.commands.loginUser.LoginUserCommand;
import com.iyte_yazilim.proje_pazari.application.commands.registerUser.RegisterUserCommand;
import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.EmailVerificationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectApplicationRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.ProjectRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.RefreshTokenRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.UserRepository;
import com.iyte_yazilim.proje_pazari.infrastructure.security.config.RateLimitConfig;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class IntegrationTestBase {
    @Autowired protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean protected StringRedisTemplate stringRedisTemplate;

    @MockitoBean protected TokenBlacklistService tokenBlacklistService;

    @MockitoBean protected JavaMailSender javaMailSender;

    @MockitoBean protected RateLimitConfig rateLimitConfig;

    @Autowired private RefreshTokenRepository refreshTokenRepository;

    @Autowired protected EmailVerificationRepository emailVerificationRepository;

    @Autowired private ProjectApplicationRepository projectApplicationRepository;

    @Autowired private ProjectRepository projectRepository;

    @Autowired protected UserRepository userRepository;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        emailVerificationRepository.deleteAll();
        projectApplicationRepository.deleteAll();
        projectRepository.deleteAll();
        userRepository.deleteAll();

        // Configure rate limiter to be permissive in tests
        Bucket permissiveBucket =
                Bucket.builder()
                        .addLimit(
                                Bandwidth.builder()
                                        .capacity(1000)
                                        .refillIntervally(1000, Duration.ofMinutes(1))
                                        .build())
                        .build();
        when(rateLimitConfig.resolveBucket(anyString())).thenReturn(permissiveBucket);
    }

    // ── Shared Helper Methods ────────────────────────────────────────────

    protected void registerAndVerifyUser(
            String email, String password, String firstName, String lastName) throws Exception {
        var command = new RegisterUserCommand(email, password, firstName, lastName);
        mockMvc.perform(
                        post("/api/v1/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(command)))
                .andExpect(status().isCreated());

        var user = userRepository.findByEmail(email).orElseThrow();
        var verification =
                emailVerificationRepository
                        .findTopByUserIdOrderByCreatedAtDesc(user.getId())
                        .orElseThrow();
        verification.setVerifiedAt(LocalDateTime.now());
        emailVerificationRepository.save(verification);
    }

    protected String loginAndGetToken(String email, String password) throws Exception {
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

    protected String createVerifiedUserAndGetToken(
            String email, String password, String firstName, String lastName) throws Exception {
        registerAndVerifyUser(email, password, firstName, lastName);
        return loginAndGetToken(email, password);
    }
}
