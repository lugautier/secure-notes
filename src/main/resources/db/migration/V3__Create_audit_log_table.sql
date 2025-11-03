-- V3__Create_audit_log_table.sql
-- Creates the audit log table for tracking user activities

CREATE TABLE audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id UUID,
    details JSONB,
    ip_address VARCHAR(45),
    user_agent VARCHAR(255),
    success BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient querying
CREATE INDEX idx_audit_log_user_id ON audit_log(user_id);
CREATE INDEX idx_audit_log_resource_type_id ON audit_log(resource_type, resource_id);
CREATE INDEX idx_audit_log_action ON audit_log(action);
CREATE INDEX idx_audit_log_created_at ON audit_log(created_at);

-- Add comments for documentation
COMMENT ON TABLE audit_log IS 'Audit trail for all user activities (auth, note CRUD)';
COMMENT ON COLUMN audit_log.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN audit_log.user_id IS 'Foreign key to users table (NULL for guest actions)';
COMMENT ON COLUMN audit_log.action IS 'Action performed (LOGIN, LOGOUT, REGISTER, CREATE_NOTE, UPDATE_NOTE, DELETE_NOTE)';
COMMENT ON COLUMN audit_log.resource_type IS 'Type of resource (USER, NOTE)';
COMMENT ON COLUMN audit_log.resource_id IS 'ID of the affected resource';
COMMENT ON COLUMN audit_log.details IS 'Additional JSON details about the action';
COMMENT ON COLUMN audit_log.ip_address IS 'Client IP address';
COMMENT ON COLUMN audit_log.user_agent IS 'Client user agent string';
COMMENT ON COLUMN audit_log.success IS 'Whether the action succeeded';
COMMENT ON COLUMN audit_log.created_at IS 'Timestamp of the action';
