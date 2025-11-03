-- V2__Create_notes_table.sql
-- Creates the notes table for encrypted note storage

CREATE TABLE notes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    encrypted_content TEXT NOT NULL,
    iv VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient queries
CREATE INDEX idx_notes_user_id ON notes(user_id);
CREATE INDEX idx_notes_is_deleted ON notes(is_deleted);
CREATE INDEX idx_notes_user_id_deleted ON notes(user_id, is_deleted);

-- Add comments for documentation
COMMENT ON TABLE notes IS 'Encrypted notes owned by users';
COMMENT ON COLUMN notes.id IS 'Unique identifier (UUID)';
COMMENT ON COLUMN notes.user_id IS 'Foreign key to users table';
COMMENT ON COLUMN notes.title IS 'Note title (plaintext)';
COMMENT ON COLUMN notes.encrypted_content IS 'AES-256-GCM encrypted content';
COMMENT ON COLUMN notes.iv IS 'Initialization vector for encryption (hex encoded)';
COMMENT ON COLUMN notes.is_deleted IS 'Soft delete flag (logical deletion)';
COMMENT ON COLUMN notes.created_at IS 'Timestamp when note was created';
COMMENT ON COLUMN notes.updated_at IS 'Timestamp when note was last updated';
