ALTER TABLE project_applications
    ADD CONSTRAINT uk_project_applications_project_user
    UNIQUE (project_id, user_id);
