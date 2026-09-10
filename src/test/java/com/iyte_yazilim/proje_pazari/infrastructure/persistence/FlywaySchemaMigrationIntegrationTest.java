package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iyte_yazilim.proje_pazari.domain.interfaces.TokenBlacklistService;
import java.util.List;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.FlywayException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

@SpringBootTest(
        webEnvironment = WebEnvironment.NONE,
        properties = {
            "spring.flyway.enabled=true",
            "spring.flyway.baseline-on-migrate=false",
            "spring.flyway.clean-disabled=true",
            "spring.jpa.hibernate.ddl-auto=validate",
            "spring.jpa.defer-datasource-initialization=false",
            "spring.sql.init.mode=never"
        })
@ActiveProfiles("test")
class FlywaySchemaMigrationIntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRESQL =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("flyway_schema_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRESQL.start();
    }

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add(
                "spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @MockitoBean private StringRedisTemplate stringRedisTemplate;
    @MockitoBean private TokenBlacklistService tokenBlacklistService;
    @MockitoBean private JavaMailSender javaMailSender;

    @Autowired private Flyway flyway;
    @Autowired private DataSource dataSource;

    @Test
    void emptyPostgresMigratesThroughV4BeforeHibernateValidation() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("4");

        List<String> tables =
                new JdbcTemplate(dataSource)
                        .queryForList(
                                """
                                SELECT table_name
                                FROM information_schema.tables
                                WHERE table_schema = 'public'
                                  AND table_type = 'BASE TABLE'
                                ORDER BY table_name
                                """,
                                String.class);

        assertThat(tables)
                .contains(
                        "audit_logs",
                        "banned_ips",
                        "email_verifications",
                        "feature_flags",
                        "flagged_content",
                        "flyway_schema_history",
                        "password_reset_tokens",
                        "pending_index",
                        "project_applications",
                        "projects",
                        "refresh_tokens",
                        "scheduled_emails",
                        "system_config",
                        "user_roles",
                        "users");
    }

    @Test
    void existingV4SchemaRequiresAnExplicitBaseline() {
        String schema = "existing_v4_copy";
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway schemaCopy = flywayForSchema(schema, false);
            schemaCopy.migrate();
            jdbc.execute("DROP TABLE " + schema + ".flyway_schema_history");

            Flyway automaticBaselineDisabled = flywayForSchema(schema, false);
            assertThatThrownBy(automaticBaselineDisabled::migrate)
                    .isInstanceOf(FlywayException.class)
                    .hasMessageContaining("non-empty schema");

            Flyway explicitV4Baseline = flywayForSchema(schema, false);
            explicitV4Baseline.baseline();

            assertThat(explicitV4Baseline.info().current().getVersion().getVersion())
                    .isEqualTo("4");
            assertThat(explicitV4Baseline.validateWithResult().validationSuccessful).isTrue();
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    @Test
    void checksumMismatchFailsValidation() {
        String schema = "checksum_failure_copy";
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        try {
            Flyway schemaCopy = flywayForSchema(schema, false);
            schemaCopy.migrate();
            jdbc.update(
                    "UPDATE "
                            + schema
                            + ".flyway_schema_history "
                            + "SET checksum = checksum + 1 WHERE version = '1'");

            assertThat(schemaCopy.validateWithResult().validationSuccessful).isFalse();
            assertThatThrownBy(schemaCopy::validate).isInstanceOf(FlywayException.class);
        } finally {
            jdbc.execute("DROP SCHEMA IF EXISTS " + schema + " CASCADE");
        }
    }

    private Flyway flywayForSchema(String schema, boolean baselineOnMigrate) {
        return Flyway.configure()
                .dataSource(dataSource)
                .schemas(schema)
                .defaultSchema(schema)
                .createSchemas(true)
                .locations("classpath:db/migration")
                .cleanDisabled(true)
                .baselineOnMigrate(baselineOnMigrate)
                .baselineVersion("4")
                .load();
    }
}
