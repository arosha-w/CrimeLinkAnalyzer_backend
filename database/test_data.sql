-- Test Data for Crime Link Analyzer Authentication System
-- Generated: November 16, 2025
-- Purpose: Testing login functionality with different user roles

-- =============================================================================
-- IMPORTANT: Password Information
-- =============================================================================
-- All test users use the same password for easy testing: TestPassword123!
-- The password hash below is BCrypt hash of "TestPassword123!"
-- 
-- In production:
-- 1. Users should have unique, strong passwords
-- 2. Force password change on first login
-- 3. Implement password policy (min length, complexity, expiration)
-- =============================================================================

-- Clear existing test data (optional - only run if you want fresh data)
-- DELETE FROM refresh_tokens;
-- DELETE FROM login_audit;
-- DELETE FROM users WHERE email LIKE '%@test.crime.gov.lk';

-- Insert test users with different roles
-- Password for all users: TestPassword123!
-- BCrypt hash: $2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.

INSERT INTO users (name, dob, gender, address, role, badge_no, email, password_hash, status) VALUES
-- 1. Admin User
('John Administrator', '1980-05-15', 'Male', '123 Police HQ Road, Colombo 01', 'Admin', 'ADMIN001', 'admin@test.crime.gov.lk', '$2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.', 'Active'),

-- 2. OIC (Officer in Charge) User
('Sarah Silva', '1985-08-22', 'Female', '456 Station Road, Kandy', 'OIC', 'OIC2024001', 'sarah.silva@test.crime.gov.lk', '$2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.', 'Active'),

-- 3. Investigator User
('Rajiv Perera', '1988-03-10', 'Male', '789 Investigation Unit, Galle', 'Investigator', 'INV2024015', 'rajiv.perera@test.crime.gov.lk', '$2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.', 'Active'),

-- 4. Field Officer User
('Nimal Fernando', '1992-11-30', 'Male', '321 Field Office, Matara', 'FieldOfficer', 'FO2024089', 'nimal.fernando@test.crime.gov.lk', '$2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.', 'Active'),

-- 5. Additional Investigator (for team testing)
('Amara Wickramasinghe', '1987-07-18', 'Female', '567 CID Office, Negombo', 'Investigator', 'INV2024032', 'amara.wickramasinghe@test.crime.gov.lk', '$2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.', 'Active'),

-- 6. Inactive User (for testing access denial)
('Kasun Inactive', '1990-02-14', 'Male', '999 Retired Street, Colombo', 'FieldOfficer', 'FO2020001', 'kasun.inactive@test.crime.gov.lk', '$2a$10$Xu5hXKvVYsLrKYvJgZ5IqeqYF3pL1wZL8WGv8c9jfKvKJ3Z1Qz1m.', 'Inactive');

-- Verify test data inserted
SELECT 
    user_id,
    name,
    role,
    badge_no,
    email,
    status,
    'TestPassword123!' as password_hint
FROM users 
WHERE email LIKE '%@test.crime.gov.lk'
ORDER BY 
    CASE role
        WHEN 'Admin' THEN 1
        WHEN 'OIC' THEN 2
        WHEN 'Investigator' THEN 3
        WHEN 'FieldOfficer' THEN 4
    END,
    name;

-- =============================================================================
-- TEST CREDENTIALS SUMMARY
-- =============================================================================
-- 
-- Role: Admin
-- Email: admin@test.crime.gov.lk
-- Password: TestPassword123!
-- Expected Route: /admin/dashboard
-- 
-- Role: OIC (Officer in Charge)
-- Email: sarah.silva@test.crime.gov.lk
-- Password: TestPassword123!
-- Expected Route: /oic/dashboard
-- 
-- Role: Investigator
-- Email: rajiv.perera@test.crime.gov.lk
-- Password: TestPassword123!
-- Expected Route: /investigator/dashboard
-- 
-- Role: Field Officer
-- Email: nimal.fernando@test.crime.gov.lk
-- Password: TestPassword123!
-- Expected Route: /field-officer/dashboard
-- 
-- Inactive User (Should Fail Login)
-- Email: kasun.inactive@test.crime.gov.lk
-- Password: TestPassword123!
-- Expected: Login should fail with "Account is not active" message
-- 
-- =============================================================================
-- TESTING CHECKLIST
-- =============================================================================
-- 
-- [ ] Test Admin Login
--     - Navigate to http://localhost:5173/login
--     - Enter: admin@test.crime.gov.lk / TestPassword123!
--     - Should redirect to /admin/dashboard
--     - Check localStorage for accessToken and refreshToken
--     - Check login_audit table for successful entry
-- 
-- [ ] Test OIC Login
--     - Login with sarah.silva@test.crime.gov.lk / TestPassword123!
--     - Should redirect to /oic/dashboard
-- 
-- [ ] Test Investigator Login
--     - Login with rajiv.perera@test.crime.gov.lk / TestPassword123!
--     - Should redirect to /investigator/dashboard
-- 
-- [ ] Test Field Officer Login
--     - Login with nimal.fernando@test.crime.gov.lk / TestPassword123!
--     - Should redirect to /field-officer/dashboard
-- 
-- [ ] Test Invalid Email
--     - Try: nonexistent@test.com / TestPassword123!
--     - Should show: "Invalid email or password"
--     - Check login_audit for failed attempt with reason "User not found"
-- 
-- [ ] Test Wrong Password
--     - Try: admin@test.crime.gov.lk / WrongPassword
--     - Should show: "Invalid email or password"
--     - Check login_audit for failed attempt with reason "Invalid password"
-- 
-- [ ] Test Inactive Account
--     - Try: kasun.inactive@test.crime.gov.lk / TestPassword123!
--     - Should show: "Account is not active"
--     - Check login_audit for failed attempt with reason "Account not active"
-- 
-- [ ] Test Token Refresh
--     - Login successfully
--     - Wait 15+ minutes (or modify JWT_EXPIRATION to 60000 for 1 min)
--     - Make an API call
--     - Should automatically refresh token without logout
-- 
-- [ ] Test Logout
--     - Login successfully
--     - Click logout button
--     - Should redirect to /login
--     - localStorage should be cleared
--     - Check refresh_tokens table - token should be revoked
-- 
-- [ ] Test Role-Based Access
--     - Login as Field Officer
--     - Try to manually navigate to /admin/dashboard
--     - Should show "Access Denied" or redirect
-- 
-- [ ] Test Audit Logging
--     - Login successfully
--     - Check login_audit table
--     - Verify: user_id, email, login_time, ip_address, user_agent, success=true
-- 
-- =============================================================================
-- QUERY TO CHECK LOGIN AUDIT LOGS
-- =============================================================================

-- View recent login attempts
SELECT 
    la.audit_id,
    la.email,
    u.name,
    u.role,
    la.login_time,
    la.success,
    la.failure_reason,
    la.ip_address,
    SUBSTRING(la.user_agent, 1, 50) as user_agent_preview
FROM login_audit la
LEFT JOIN users u ON la.user_id = u.user_id
ORDER BY la.login_time DESC
LIMIT 20;

-- Count login attempts by user
SELECT 
    u.name,
    u.email,
    u.role,
    COUNT(*) as total_attempts,
    SUM(CASE WHEN la.success THEN 1 ELSE 0 END) as successful_logins,
    SUM(CASE WHEN NOT la.success THEN 1 ELSE 0 END) as failed_logins
FROM login_audit la
JOIN users u ON la.user_id = u.user_id
WHERE u.email LIKE '%@test.crime.gov.lk'
GROUP BY u.user_id, u.name, u.email, u.role
ORDER BY total_attempts DESC;

-- =============================================================================
-- QUERY TO CHECK ACTIVE REFRESH TOKENS
-- =============================================================================

SELECT 
    rt.token_id,
    u.name,
    u.email,
    u.role,
    rt.created_at,
    rt.expiry_date,
    rt.revoked,
    CASE 
        WHEN rt.expiry_date < NOW() THEN 'EXPIRED'
        WHEN rt.revoked THEN 'REVOKED'
        ELSE 'ACTIVE'
    END as token_status
FROM refresh_tokens rt
JOIN users u ON rt.user_id = u.user_id
WHERE u.email LIKE '%@test.crime.gov.lk'
ORDER BY rt.created_at DESC;

-- =============================================================================
-- CLEANUP QUERIES (Use when you want to reset test data)
-- =============================================================================

-- Clear all refresh tokens for test users
-- DELETE FROM refresh_tokens WHERE user_id IN (SELECT user_id FROM users WHERE email LIKE '%@test.crime.gov.lk');

-- Clear all login audit entries for test users
-- DELETE FROM login_audit WHERE email LIKE '%@test.crime.gov.lk';

-- Remove all test users
-- DELETE FROM users WHERE email LIKE '%@test.crime.gov.lk';

-- =============================================================================
-- ADDITIONAL TEST SCENARIOS
-- =============================================================================

-- Test Password Migration (Plain Text to BCrypt)
-- 1. Insert a user with plain text password:
-- INSERT INTO users (name, dob, gender, address, role, badge_no, email, password_hash, status) 
-- VALUES ('Test Migration', '1990-01-01', 'Male', 'Test Address', 'FieldOfficer', 'TEST001', 
--         'migration@test.crime.gov.lk', 'PlainTextPassword', 'Active');
-- 
-- 2. Try to login with: migration@test.crime.gov.lk / PlainTextPassword
-- 3. System should detect plain text, hash it with BCrypt, and update database
-- 4. Verify password_hash is now BCrypt format: SELECT password_hash FROM users WHERE email = 'migration@test.crime.gov.lk';
-- 5. Try logging in again - should still work with same password

-- =============================================================================
-- SECURITY NOTES
-- =============================================================================
-- 
-- 1. JWT Secret: Secure 512-bit key is now set in .env file
-- 2. Password Storage: BCrypt with automatic salt
-- 3. Session Management: Stateless JWT with refresh tokens
-- 4. Audit Trail: All login attempts logged with IP and user agent
-- 5. Token Expiry: Access tokens expire in 15 minutes, refresh in 7 days
-- 6. Account Status: Inactive accounts cannot login
-- 
-- PRODUCTION RECOMMENDATIONS:
-- - Force password change on first login
-- - Implement rate limiting (5 attempts per 15 minutes)
-- - Enable HTTPS only
-- - Add MFA for Admin and OIC roles
-- - Regular audit log reviews
-- - Automated alerts for suspicious activity
-- - Password complexity requirements
-- - Account lockout after N failed attempts
-- 
-- =============================================================================
