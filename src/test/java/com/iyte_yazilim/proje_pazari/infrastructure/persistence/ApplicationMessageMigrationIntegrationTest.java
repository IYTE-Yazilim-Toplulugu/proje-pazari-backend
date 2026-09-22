package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class ApplicationMessageMigrationIntegrationTest extends IntegrationTestBase {

    private static final String SCHEMA = "application_message_v6_test";

    @Autowired private DataSource dataSource;

    @Test
    void v6EnforcesBodyConstraintsOrderingIndexAndDocumentedDeleteBehavior() throws Exception {
        try (Connection connection = dataSource.getConnection();
                Statement statement = connection.createStatement()) {
            try {
                statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
                statement.execute("CREATE SCHEMA " + SCHEMA);
                connection.setSchema(SCHEMA);
                statement.execute("CREATE TABLE users (id VARCHAR(26) PRIMARY KEY)");
                statement.execute("CREATE TABLE project_applications (id VARCHAR(26) PRIMARY KEY)");

                ScriptUtils.executeSqlScript(
                        connection,
                        new ClassPathResource("db/migration/V6__add_application_messages.sql"));

                statement.executeUpdate("INSERT INTO users (id) VALUES ('sender-1')");
                statement.executeUpdate(
                        "INSERT INTO project_applications (id) VALUES ('application-1')");
                statement.executeUpdate(
                        "INSERT INTO application_messages"
                                + " (id, application_id, sender_id, body) VALUES"
                                + " ('message-1', 'application-1', 'sender-1', 'hello')");

                assertThatThrownBy(
                                () ->
                                        statement.executeUpdate(
                                                "INSERT INTO application_messages"
                                                        + " (id, application_id, sender_id, body)"
                                                        + " VALUES ('blank', 'application-1',"
                                                        + " 'sender-1', '   ')"))
                        .isInstanceOf(java.sql.SQLException.class);

                try (PreparedStatement overLimit =
                        connection.prepareStatement(
                                "INSERT INTO application_messages"
                                        + " (id, application_id, sender_id, body)"
                                        + " VALUES ('too-long', 'application-1', 'sender-1', ?)")) {
                    overLimit.setString(1, "x".repeat(2_001));
                    assertThatThrownBy(overLimit::executeUpdate)
                            .isInstanceOf(java.sql.SQLException.class);
                }

                assertThatThrownBy(
                                () ->
                                        statement.executeUpdate(
                                                "DELETE FROM users WHERE id = 'sender-1'"))
                        .isInstanceOf(java.sql.SQLException.class);

                try (ResultSet index =
                        statement.executeQuery(
                                "SELECT indexdef FROM pg_indexes"
                                        + " WHERE schemaname = '"
                                        + SCHEMA
                                        + "' AND indexname ="
                                        + " 'idx_application_messages_thread_order'")) {
                    assertThat(index.next()).isTrue();
                    assertThat(index.getString("indexdef"))
                            .contains("application_id", "created_at", "id");
                }

                statement.executeUpdate(
                        "DELETE FROM project_applications WHERE id = 'application-1'");
                try (ResultSet count =
                        statement.executeQuery("SELECT COUNT(*) FROM application_messages")) {
                    assertThat(count.next()).isTrue();
                    assertThat(count.getInt(1)).isZero();
                }
                assertThat(statement.executeUpdate("DELETE FROM users WHERE id = 'sender-1'"))
                        .isEqualTo(1);
            } finally {
                connection.setSchema("public");
            }
        } finally {
            try (Connection cleanup = dataSource.getConnection();
                    Statement statement = cleanup.createStatement()) {
                statement.execute("DROP SCHEMA IF EXISTS " + SCHEMA + " CASCADE");
            }
        }
    }
}
