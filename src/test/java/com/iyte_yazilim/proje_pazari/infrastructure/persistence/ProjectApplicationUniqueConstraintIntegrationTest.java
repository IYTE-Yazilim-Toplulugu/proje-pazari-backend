package com.iyte_yazilim.proje_pazari.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.iyte_yazilim.proje_pazari.IntegrationTestBase;
import com.iyte_yazilim.proje_pazari.domain.enums.ApplicationStatus;
import com.iyte_yazilim.proje_pazari.domain.enums.ProjectStatus;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectApplicationEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.ProjectEntity;
import com.iyte_yazilim.proje_pazari.infrastructure.persistence.models.UserEntity;
import java.sql.Connection;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;

class ProjectApplicationUniqueConstraintIntegrationTest extends IntegrationTestBase {

    private static final String CONSTRAINT_NAME = "uk_project_applications_project_user";

    @Autowired private DataSource dataSource;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectApplicationRepository applicationRepository;

    @Test
    void persistenceSchemaRejectsDuplicateProjectAndUserApplications() {
        ProjectEntity project = persistProjectAndApplicant();
        UserEntity applicant =
                userRepository.findByEmail("constraint@applicant.test").orElseThrow();

        applicationRepository.saveAndFlush(application(project, applicant));

        assertThatThrownBy(
                        () -> applicationRepository.saveAndFlush(application(project, applicant)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void v5MigrationInstallsTheNamedUniqueConstraint() throws Exception {
        jdbcTemplate.execute(
                "ALTER TABLE project_applications DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME);

        try (Connection connection = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "db/migration/V5__enforce_unique_project_applications.sql"));
        }

        Integer constraintCount =
                jdbcTemplate.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.table_constraints
                        WHERE table_schema = current_schema()
                          AND table_name = 'project_applications'
                          AND constraint_name = ?
                          AND constraint_type = 'UNIQUE'
                        """,
                        Integer.class,
                        CONSTRAINT_NAME);

        assertThat(constraintCount).isEqualTo(1);
    }

    private ProjectEntity persistProjectAndApplicant() {
        UserEntity owner = user("constraint@owner.test");
        UserEntity applicant = user("constraint@applicant.test");
        userRepository.saveAndFlush(owner);
        userRepository.saveAndFlush(applicant);

        ProjectEntity project = new ProjectEntity();
        project.setTitle("Constraint Project");
        project.setStatus(ProjectStatus.OPEN);
        project.setOwner(owner);
        project.setMaxTeamSize(3);
        project.setCurrentTeamSize(1);
        return projectRepository.saveAndFlush(project);
    }

    private static ProjectApplicationEntity application(
            ProjectEntity project, UserEntity applicant) {
        ProjectApplicationEntity application = new ProjectApplicationEntity();
        application.setProject(project);
        application.setUser(applicant);
        application.setStatus(ApplicationStatus.PENDING);
        return application;
    }

    private static UserEntity user(String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPassword("encoded-password");
        user.setFirstName("Constraint");
        user.setLastName("User");
        return user;
    }
}
