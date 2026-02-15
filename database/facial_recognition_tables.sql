-- ====================================================================
-- Facial Recognition System - Database Schema
-- ====================================================================
-- This schema supports the facial recognition feature for CrimeLinkAnalyzer
-- Created: December 11, 2025
-- ====================================================================

-- Drop existing tables if they exist (for clean setup)
DROP TABLE IF EXISTS facial_recognition_logs CASCADE;
DROP TABLE IF EXISTS suspect_photos CASCADE;
DROP TABLE IF EXISTS criminals CASCADE;

-- ====================================================================
-- CRIMINALS TABLE
-- ====================================================================
-- Stores criminal records with biometric data
CREATE TABLE criminals (
    criminal_id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    nic VARCHAR(20) UNIQUE,
    alias VARCHAR(255),
    date_of_birth DATE,
    gender VARCHAR(10) CHECK (gender IN ('Male', 'Female', 'Other')),
    address TEXT,
    nationality VARCHAR(100) DEFAULT 'Sri Lankan',
    
    -- Crime information stored as JSONB for flexibility
    crime_history JSONB DEFAULT '[]'::jsonb,
    
    -- Primary photo reference
    primary_photo_url VARCHAR(500),
    
    -- Face embedding - stored as BYTEA (binary)
    -- This is the average embedding from all photos
    face_embedding BYTEA,
    embedding_model VARCHAR(50) DEFAULT 'buffalo_sc',
    embedding_dimension INTEGER DEFAULT 512,
    
    -- Status tracking
    status VARCHAR(20) DEFAULT 'active' CHECK (status IN ('active', 'inactive', 'archived')),
    risk_level VARCHAR(20) DEFAULT 'medium' CHECK (risk_level IN ('low', 'medium', 'high', 'critical')),
    
    -- Audit fields
    created_by INTEGER, -- User ID who created this record
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Metadata
    notes TEXT,
    last_seen_location VARCHAR(255),
    last_seen_date DATE
);

-- ====================================================================
-- SUSPECT_PHOTOS TABLE
-- ====================================================================
-- Stores multiple photos per criminal for better accuracy
CREATE TABLE suspect_photos (
    photo_id SERIAL PRIMARY KEY,
    criminal_id INTEGER NOT NULL REFERENCES criminals(criminal_id) ON DELETE CASCADE,
    
    -- Photo storage
    photo_url VARCHAR(500) NOT NULL,
    photo_hash VARCHAR(64) UNIQUE, -- SHA-256 hash to prevent duplicates
    file_size_bytes INTEGER,
    
    -- Face detection metadata
    face_embedding BYTEA NOT NULL, -- Individual photo embedding
    face_confidence DECIMAL(5,2), -- Detection confidence (0-100)
    face_bbox JSONB, -- Bounding box coordinates {x, y, width, height}
    
    -- Photo metadata
    is_primary BOOLEAN DEFAULT FALSE,
    photo_quality VARCHAR(20) CHECK (photo_quality IN ('low', 'medium', 'high', 'excellent')),
    image_width INTEGER,
    image_height INTEGER,
    
    -- Source tracking
    source VARCHAR(100), -- e.g., 'manual_upload', 'cctv', 'arrest_record'
    source_date DATE,
    
    -- Audit
    uploaded_by INTEGER, -- User ID
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT only_one_primary_per_criminal 
        EXCLUDE USING gist (criminal_id WITH =) 
        WHERE (is_primary = true)
);

-- ====================================================================
-- FACIAL_RECOGNITION_LOGS TABLE
-- ====================================================================
-- Audit trail for all facial recognition requests
CREATE TABLE facial_recognition_logs (
    log_id SERIAL PRIMARY KEY,
    
    -- Request details
    analysis_type VARCHAR(50) DEFAULT 'suspect_match', -- 'suspect_match', 'criminal_registration'
    uploaded_image_url VARCHAR(500),
    uploaded_image_hash VARCHAR(64),
    
    -- Face detection results
    face_detected BOOLEAN DEFAULT FALSE,
    face_count INTEGER DEFAULT 0,
    face_quality VARCHAR(20),
    
    -- Matching results
    matches_found INTEGER DEFAULT 0,
    best_match_criminal_id INTEGER REFERENCES criminals(criminal_id),
    best_match_similarity DECIMAL(5,2), -- Percentage (0-100)
    match_threshold DECIMAL(5,2) DEFAULT 75.00,
    
    -- All matches stored as JSONB for detailed analysis
    all_matches JSONB DEFAULT '[]'::jsonb,
    
    -- Performance metrics
    processing_time_ms INTEGER,
    model_version VARCHAR(50),
    
    -- Security & Audit
    requested_by INTEGER, -- User ID
    user_role VARCHAR(50),
    ip_address INET,
    user_agent TEXT,
    
    -- Timestamps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Investigation reference
    case_id VARCHAR(100),
    investigation_notes TEXT
);

-- ====================================================================
-- INDEXES FOR PERFORMANCE
-- ====================================================================

-- Criminal table indexes
CREATE INDEX idx_criminal_nic ON criminals(nic);
CREATE INDEX idx_criminal_name ON criminals USING gin(to_tsvector('english', name));
CREATE INDEX idx_criminal_status ON criminals(status) WHERE status = 'active';
CREATE INDEX idx_criminal_risk_level ON criminals(risk_level);
CREATE INDEX idx_criminal_created_at ON criminals(created_at DESC);

-- Suspect photos indexes
CREATE INDEX idx_suspect_photos_criminal_id ON suspect_photos(criminal_id);
CREATE INDEX idx_suspect_photos_primary ON suspect_photos(criminal_id, is_primary) WHERE is_primary = true;
CREATE INDEX idx_suspect_photos_hash ON suspect_photos(photo_hash);

-- Facial recognition logs indexes
CREATE INDEX idx_fr_logs_created_at ON facial_recognition_logs(created_at DESC);
CREATE INDEX idx_fr_logs_user ON facial_recognition_logs(requested_by);
CREATE INDEX idx_fr_logs_best_match ON facial_recognition_logs(best_match_criminal_id) WHERE best_match_criminal_id IS NOT NULL;
CREATE INDEX idx_fr_logs_case_id ON facial_recognition_logs(case_id) WHERE case_id IS NOT NULL;

-- ====================================================================
-- TRIGGERS FOR AUTO-UPDATE
-- ====================================================================

-- Automatically update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_criminals_updated_at
    BEFORE UPDATE ON criminals
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ====================================================================
-- UTILITY FUNCTIONS
-- ====================================================================

-- Function to calculate average embedding from multiple photos
CREATE OR REPLACE FUNCTION calculate_average_embedding(p_criminal_id INTEGER)
RETURNS BYTEA AS $$
DECLARE
    avg_embedding BYTEA;
BEGIN
    -- This will be called from Python after uploading multiple photos
    -- Python will handle the actual embedding averaging logic
    -- This function is a placeholder for future stored procedure implementation
    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- Function to search similar faces (placeholder - actual search done in Python)
CREATE OR REPLACE FUNCTION search_similar_faces(
    p_embedding BYTEA,
    p_threshold DECIMAL DEFAULT 0.75,
    p_limit INTEGER DEFAULT 10
)
RETURNS TABLE (
    criminal_id INTEGER,
    name VARCHAR,
    similarity DECIMAL
) AS $$
BEGIN
    -- Actual similarity search is performed in Python using numpy
    -- This is a placeholder for documentation
    RETURN QUERY SELECT NULL::INTEGER, NULL::VARCHAR, NULL::DECIMAL LIMIT 0;
END;
$$ LANGUAGE plpgsql;

-- ====================================================================
-- INITIAL DATA / SEED DATA
-- ====================================================================

-- Insert sample criminal record for testing
INSERT INTO criminals (
    name, 
    nic, 
    date_of_birth, 
    gender, 
    crime_history,
    status,
    risk_level,
    notes
) VALUES (
    'Test Suspect One',
    '199012345678',
    '1990-05-15',
    'Male',
    '[{"crime_type": "Theft", "date": "2023-03-10", "status": "Convicted", "sentence": "2 years"}]'::jsonb,
    'active',
    'medium',
    'Sample criminal record for testing facial recognition system'
);

-- ====================================================================
-- PERMISSIONS & SECURITY
-- ====================================================================

-- Grant appropriate permissions (adjust based on your user roles)
-- GRANT SELECT, INSERT, UPDATE ON criminals TO crimelink_app_user;
-- GRANT SELECT, INSERT ON suspect_photos TO crimelink_app_user;
-- GRANT INSERT ON facial_recognition_logs TO crimelink_app_user;

-- ====================================================================
-- COMMENTS FOR DOCUMENTATION
-- ====================================================================

COMMENT ON TABLE criminals IS 'Stores criminal records with biometric face embeddings for facial recognition';
COMMENT ON COLUMN criminals.face_embedding IS 'Average face embedding vector stored as binary data (512-dimensional float32 array)';
COMMENT ON COLUMN criminals.crime_history IS 'JSON array of crime records: [{crime_type, date, status, sentence}]';

COMMENT ON TABLE suspect_photos IS 'Multiple photos per criminal for improved recognition accuracy';
COMMENT ON COLUMN suspect_photos.face_embedding IS 'Individual face embedding for this specific photo';
COMMENT ON COLUMN suspect_photos.photo_hash IS 'SHA-256 hash to prevent duplicate photo uploads';

COMMENT ON TABLE facial_recognition_logs IS 'Audit trail for all facial recognition analysis requests';
COMMENT ON COLUMN facial_recognition_logs.all_matches IS 'JSON array of all matches: [{criminal_id, similarity, confidence}]';

-- ====================================================================
-- END OF SCHEMA
-- ====================================================================
