CREATE TABLE pending_index (
    id VARCHAR(26) PRIMARY KEY,
    project_id VARCHAR(26) NOT NULL,
    attempt_count INT NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL
);

-- At most one live (PENDING) retry entry per project, preventing duplicate rows
-- when a project repeatedly fails to index during an outage.
CREATE UNIQUE INDEX ux_pending_index_project_pending
    ON pending_index (project_id)
    WHERE status = 'PENDING';

-- Supports the scheduled retry loop's lookup by status.
CREATE INDEX ix_pending_index_status ON pending_index (status);
