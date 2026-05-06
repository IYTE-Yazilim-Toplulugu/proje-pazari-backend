package com.iyte_yazilim.proje_pazari.presentation.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Integration-level startup tests for {@link JwtSecretValidator}.
 *
 * <p>Uses {@link ApplicationContextRunner} to exercise the real Spring {@code @PostConstruct}
 * lifecycle, verifying that the application context refuses to start on an insecure JWT secret and
 * starts successfully on a valid one — without loading the full application context.
 */
class JwtSecretValidatorStartupTest {

    private final ApplicationContextRunner contextRunner =
            new ApplicationContextRunner().withUserConfiguration(JwtSecretValidator.class);

    @Test
    void contextFailsToStartWhenSecretContainsPlaceholderIndicator() {
        contextRunner
                .withPropertyValues(
                        "jwt.secret=change-this-in-production-insecure-default-placeholder")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure().getCause())
                                    .isInstanceOf(IllegalStateException.class)
                                    .hasMessageContaining("JWT_SECRET");
                        });
    }

    @Test
    void contextFailsToStartWhenSecretIsTooShort() {
        contextRunner
                .withPropertyValues("jwt.secret=too-short")
                .run(
                        context -> {
                            assertThat(context).hasFailed();
                            assertThat(context.getStartupFailure().getCause())
                                    .isInstanceOf(IllegalStateException.class)
                                    .hasMessageContaining("32");
                        });
    }

    @Test
    void contextStartsSuccessfullyWithValidSecret() {
        contextRunner
                .withPropertyValues(
                        "jwt.secret=a-secure-random-secret-that-is-at-least-32-characters-long")
                .run(
                        context -> {
                            assertThat(context).hasNotFailed();
                            assertThat(context).hasSingleBean(JwtSecretValidator.class);
                        });
    }
}
