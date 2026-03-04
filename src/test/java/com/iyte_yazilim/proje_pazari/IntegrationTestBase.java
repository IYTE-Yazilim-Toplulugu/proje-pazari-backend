package com.iyte_yazilim.proje_pazari;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @Autowired private EmailVerificationRepository emailVerificationRepository;

    @Autowired private ProjectApplicationRepository projectApplicationRepository;

    @Autowired private ProjectRepository projectRepository;

    @Autowired private UserRepository userRepository;

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
}
