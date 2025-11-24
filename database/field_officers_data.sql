-- ========================================
-- Field Officers Test Data
-- Crime Link Analyzer
-- ========================================
-- This script inserts 10 Field Officer users into the users table
-- Password for all users: TestPassword123!
-- BCrypt Hash: $2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa
-- ========================================

-- Insert 10 Field Officers
INSERT INTO users (name, dob, gender, address, role, badge_no, email, password_hash, status) VALUES
-- Field Officer 1
('Kamal Bandara', '1990-03-15', 'Male', '45 Police Station Road, Matara', 'FieldOfficer', 'FO2024001', 'kamal.bandara@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 2
('Nimal Fernando', '1992-11-30', 'Male', '321 Field Office, Galle', 'FieldOfficer', 'FO2024002', 'nimal.fernando@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 3
('Chaminda Silva', '1988-07-22', 'Male', '78 Station Road, Kandy', 'FieldOfficer', 'FO2024003', 'chaminda.silva@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 4
('Dilini Rathnayake', '1995-05-18', 'Female', '156 Police Division, Kurunegala', 'FieldOfficer', 'FO2024004', 'dilini.rathnayake@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 5
('Thilina Jayawardena', '1991-09-12', 'Male', '234 CID Unit, Negombo', 'FieldOfficer', 'FO2024005', 'thilina.jayawardena@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 6
('Anusha Wickramasinghe', '1993-01-25', 'Female', '89 Police Post, Anuradhapura', 'FieldOfficer', 'FO2024006', 'anusha.wickramasinghe@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 7
('Roshan Perera', '1989-12-08', 'Male', '67 Field Station, Jaffna', 'FieldOfficer', 'FO2024007', 'roshan.perera@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 8
('Sanduni Dissanayake', '1994-06-20', 'Female', '123 Police Division, Batticaloa', 'FieldOfficer', 'FO2024008', 'sanduni.dissanayake@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 9
('Asanka Gamage', '1987-04-03', 'Male', '45 Police Unit, Trincomalee', 'FieldOfficer', 'FO2024009', 'asanka.gamage@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active'),

-- Field Officer 10
('Madhavi Gunasekara', '1996-08-14', 'Female', '98 Field Office, Hambantota', 'FieldOfficer', 'FO2024010', 'madhavi.gunasekara@crime.gov.lk', '$2b$10$.K0c43M3awqRHBlK0uKKmemz9PTX6vM3fjydAnecDcYmPUfsD6aFa', 'Active')

ON CONFLICT (email) DO NOTHING;

-- ========================================
-- Verification Query
-- ========================================
-- Run this to verify the Field Officers were inserted:
-- SELECT name, email, role, badge_no, status FROM users WHERE role = 'FieldOfficer' ORDER BY badge_no;

-- ========================================
-- Field Officers Summary
-- ========================================
-- Total: 10 Field Officers
-- Badge Numbers: FO2024001 to FO2024010
-- Locations: Matara, Galle, Kandy, Kurunegala, Negombo, Anuradhapura, Jaffna, Batticaloa, Trincomalee, Hambantota
-- Gender Distribution: 6 Male, 4 Female
-- All Status: Active
-- Password: TestPassword123! (for all)
-- ========================================

-- ========================================
-- Insert Instructions
-- ========================================
-- Execute this script with:
-- psql -h <host> -p <port> -U <username> -d <database> -f field_officers_data.sql
--
-- Or with environment variable:
-- $env:PGPASSWORD="<password>"; psql -h <host> -p <port> -U <username> -d <database> -f field_officers_data.sql
-- ========================================
