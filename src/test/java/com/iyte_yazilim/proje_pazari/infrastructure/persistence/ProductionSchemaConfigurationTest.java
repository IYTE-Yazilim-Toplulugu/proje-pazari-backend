package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class ProductionSchemaConfigurationTest {

    @Test
    void productionAndStagingUseFlywayThenHibernateValidationWithoutSampleData()
            throws IOException {
        for (String profile : new String[] {"prod", "staging"}) {
            String properties =
                    new ClassPathResource("application-" + profile + ".properties")
                            .getContentAsString(StandardCharsets.UTF_8);

            assertThat(properties)
                    .contains("spring.flyway.enabled=true")
                    .contains("spring.flyway.validate-on-migrate=true")
                    .contains("spring.flyway.baseline-on-migrate=false")
                    .contains("spring.flyway.clean-disabled=true")
                    .contains("spring.jpa.hibernate.ddl-auto=validate")
                    .contains("spring.jpa.defer-datasource-initialization=false")
                    .contains("spring.sql.init.mode=never")
                    .doesNotContain("spring.jpa.hibernate.ddl-auto=update");
        }
    }

    @Test
    void developmentKeepsFlywayOptIn() throws IOException {
        String properties = Files.readString(Path.of("src/main/resources/application.properties"));
        String testProperties =
                Files.readString(Path.of("src/test/resources/application.properties"));

        assertThat(properties).contains("spring.flyway.enabled=false");
        assertThat(testProperties).contains("spring.flyway.enabled=false");
    }

    @Test
    void composeCannotOverrideProductionBackToHibernateMutation() throws IOException {
        String compose = Files.readString(Path.of("docker-compose.yml"));

        assertThat(compose)
                .contains("SPRING_JPA_HIBERNATE_DDL_AUTO: validate")
                .contains("SPRING_FLYWAY_ENABLED: \"true\"")
                .contains("SPRING_FLYWAY_VALIDATE_ON_MIGRATE: \"true\"")
                .contains("SPRING_FLYWAY_BASELINE_ON_MIGRATE: \"false\"")
                .contains("SPRING_FLYWAY_CLEAN_DISABLED: \"true\"")
                .contains("SPRING_SQL_INIT_MODE: never")
                .doesNotContain("SPRING_JPA_HIBERNATE_DDL_AUTO: update");
    }

    @Test
    void migrationOperationsUseApplicationStartupAndCiHasAnExplicitSchemaCheck()
            throws IOException {
        assertThat(Files.readString(Path.of("Makefile")))
                .contains("db-migrate:")
                .contains("docker compose up -d --wait --wait-timeout 120 app");

        for (String workflow : new String[] {"pr-validation.yml", "dev-ci-cd.yml", "prod-cd.yml"}) {
            assertThat(Files.readString(Path.of(".github/workflows", workflow)))
                    .contains("Verify clean Flyway migration and Hibernate validation")
                    .contains("FlywaySchemaMigrationIntegrationTest");
        }

        assertThat(Files.readString(Path.of(".github/workflows/prod-cd.yml")))
                .contains("up -d --no-deps --wait --wait-timeout 120 app");
    }
}
