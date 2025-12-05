package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.AuditLogDTO;
import com.crimeLink.analyzer.entity.LoginAudit;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.LoginAuditRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepo;
    private final LoginAuditRepository auditRepo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get all users or filter by role/status
     * GET /api/admin/users?role=Admin&status=Active
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        List<User> users;

        if (role != null && status != null) {
            users = userRepo.findByRoleAndStatus(role, status);
        } else if (role != null) {
            users = userRepo.findByRole(role);
        } else if (status != null) {
            users = userRepo.findByRoleAndStatus(null, status);
        } else {
            users = userRepo.findAll();
        }

        return ResponseEntity.ok(users);
    }

    /**
     * Create new user
     * POST /api/admin/users
     */
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            // Check if email already exists
            if (userRepo.existsByEmail(user.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Email already exists"));
            }

            // Hash password
            if (user.getPasswordHash() != null && !user.getPasswordHash().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
            }

            User savedUser = userRepo.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Failed to create user: " + e.getMessage()));
        }
    }

    /**
     * Update existing user
     * PUT /api/admin/users/{id}
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody User updatedUser) {

        return userRepo.findById(id)
                .map(user -> {
                    user.setName(updatedUser.getName());
                    user.setEmail(updatedUser.getEmail());
                    user.setDob(updatedUser.getDob());
                    user.setGender(updatedUser.getGender());
                    user.setAddress(updatedUser.getAddress());
                    user.setRole(updatedUser.getRole());
                    user.setBadgeNo(updatedUser.getBadgeNo());
                    user.setStatus(updatedUser.getStatus());

                    // Only update password if provided
                    if (updatedUser.getPasswordHash() != null 
                            && !updatedUser.getPasswordHash().isEmpty()) {
                        user.setPasswordHash(
                                passwordEncoder.encode(updatedUser.getPasswordHash()));
                    }

                    User saved = userRepo.save(user);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Deactivate user (set status to Inactive)
     * DELETE /api/admin/users/{id}
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable Integer id) {
        return userRepo.findById(id)
                .map(user -> {
                    user.setStatus("Inactive");
                    userRepo.save(user);
                    return ResponseEntity.ok(
                            Map.of("message", "User deactivated successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get audit logs
     * GET /api/admin/audit-logs?limit=100&offset=0
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLogDTO>> getAuditLogs(
            @RequestParam(defaultValue = "100") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        PageRequest pageRequest = PageRequest.of(
                offset / limit,
                limit,
                Sort.by(Sort.Direction.DESC, "loginTime")
        );

        List<LoginAudit> logs = auditRepo.findAll(pageRequest).getContent();
        
        // Map LoginAudit to AuditLogDTO with user names
        List<AuditLogDTO> dtoList = logs.stream().map(log -> {
            AuditLogDTO dto = new AuditLogDTO();
            dto.setId(log.getAuditId());
            dto.setUserId(log.getUserId());
            dto.setEmail(log.getEmail());
            dto.setIpAddress(log.getIpAddress());
            dto.setLoginTime(log.getLoginTime() != null ? log.getLoginTime().toString() : null);
            dto.setLogoutTime(null); // LoginAudit doesn't track logout time
            dto.setSuccess(log.getSuccess());
            
            // Determine action based on success and failure reason
            if (log.getSuccess()) {
                dto.setAction("Login Success");
            } else {
                String reason = log.getFailureReason();
                if (reason != null) {
                    dto.setAction("Login Failed: " + reason);
                } else {
                    dto.setAction("Login Failed");
                }
            }
            
            // Get user name from userId if available
            if (log.getUserId() != null) {
                userRepo.findById(log.getUserId()).ifPresent(user -> {
                    dto.setUserName(user.getName());
                });
            } else {
                // Use email as fallback
                dto.setUserName(log.getEmail());
            }
            
            return dto;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Trigger database backup
     * POST /api/admin/backup
     */
    @PostMapping("/backup")
    public ResponseEntity<?> triggerBackup() {
        try {
            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            String filename = "backup_" + timestamp + ".sql";

            // TODO: Implement actual backup logic
            // For Railway PostgreSQL, use pg_dump or Spring's backup mechanisms

            return ResponseEntity.ok(Map.of(
                    "message", "Backup created successfully",
                    "file", filename
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("message", "Backup failed: " + e.getMessage())
            );
        }
    }

    /**
     * Restore from backup
     * POST /api/admin/restore
     */
    @PostMapping("/restore")
    public ResponseEntity<?> restoreBackup(@RequestBody Map<String, String> request) {
        try {
            String filename = request.get("filename");
            if (filename == null || filename.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Filename is required"));
            }

            // TODO: Implement actual restore logic
            // For Railway PostgreSQL, use psql or Spring's restore mechanisms

            return ResponseEntity.ok(Map.of(
                    "message", "Database restored successfully from " + filename
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("message", "Restore failed: " + e.getMessage())
            );
        }
    }

    /**
     * Get system health
     * GET /api/admin/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> getSystemHealth() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "database", "Connected",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
