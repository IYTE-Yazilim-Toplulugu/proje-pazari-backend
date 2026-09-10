-- Supported relational baseline before V2 (project optimistic locking),
-- V3 (application review messages), and V4 (index retry queue).

CREATE TABLE users (
    id VARCHAR(26) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    description TEXT,
    profile_picture_url VARCHAR(255),
    linkedin_url VARCHAR(255),
    github_url VARCHAR(255),
    preferred_language VARCHAR(5),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    two_factor_secret VARCHAR(255),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id VARCHAR(26) NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_roles_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE projects (
    id VARCHAR(26) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    summary TEXT,
    status VARCHAR(255) NOT NULL,
    owner_id VARCHAR(26) NOT NULL,
    max_team_size INTEGER,
    current_team_size INTEGER,
    category VARCHAR(255),
    deadline TIMESTAMP,
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_projects_owner
        FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE INDEX idx_projects_owner_id ON projects (owner_id);

CREATE TABLE project_entity_required_skills (
    project_entity_id VARCHAR(26) NOT NULL,
    required_skill VARCHAR(255),
    CONSTRAINT fk_project_required_skills_project
        FOREIGN KEY (project_entity_id) REFERENCES projects (id)
);

CREATE TABLE project_applications (
    id VARCHAR(26) PRIMARY KEY,
    project_id VARCHAR(26) NOT NULL,
    user_id VARCHAR(26) NOT NULL,
    status VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_project_applications_project
        FOREIGN KEY (project_id) REFERENCES projects (id),
    CONSTRAINT fk_project_applications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_proj_apps_project_id ON project_applications (project_id);
CREATE INDEX idx_proj_apps_user_id ON project_applications (user_id);

CREATE TABLE email_verifications (
    id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    verified_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_email_verifications_token UNIQUE (token)
);

CREATE TABLE password_reset_tokens (
    id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    token VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_prt_token UNIQUE (token)
);

CREATE INDEX idx_prt_user_id ON password_reset_tokens (user_id);

CREATE TABLE refresh_tokens (
    id VARCHAR(255) PRIMARY KEY,
    token VARCHAR(500) NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_refresh_tokens_token UNIQUE (token)
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE TABLE audit_logs (
    id VARCHAR(26) PRIMARY KEY,
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255),
    entity_id VARCHAR(255),
    performed_by VARCHAR(255) NOT NULL,
    ip_address VARCHAR(255),
    timestamp TIMESTAMP NOT NULL,
    details TEXT,
    status VARCHAR(255) NOT NULL
);

CREATE INDEX idx_audit_logs_performed_by ON audit_logs (performed_by);
CREATE INDEX idx_audit_logs_timestamp ON audit_logs (timestamp);

CREATE TABLE banned_ips (
    id VARCHAR(26) PRIMARY KEY,
    ip_address VARCHAR(255) NOT NULL,
    reason VARCHAR(255),
    banned_by VARCHAR(255),
    banned_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    CONSTRAINT uk_banned_ips_ip_address UNIQUE (ip_address)
);

CREATE INDEX idx_banned_ips_ip_address ON banned_ips (ip_address);

CREATE TABLE feature_flags (
    id VARCHAR(26) PRIMARY KEY,
    flag_key VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT FALSE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uk_feature_flags_flag_key UNIQUE (flag_key)
);

CREATE TABLE system_config (
    id VARCHAR(26) PRIMARY KEY,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    description VARCHAR(255),
    updated_at TIMESTAMP,
    CONSTRAINT uk_system_config_config_key UNIQUE (config_key)
);

CREATE TABLE scheduled_emails (
    id VARCHAR(26) PRIMARY KEY,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    target_role VARCHAR(255),
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL
);

CREATE TABLE flagged_content (
    id VARCHAR(26) PRIMARY KEY,
    content_type VARCHAR(255) NOT NULL,
    content_id VARCHAR(255) NOT NULL,
    reason VARCHAR(255) NOT NULL,
    reported_by VARCHAR(255) NOT NULL,
    status VARCHAR(255) NOT NULL,
    reviewed_by VARCHAR(255),
    review_note TEXT,
    created_at TIMESTAMP NOT NULL,
    reviewed_at TIMESTAMP
);
