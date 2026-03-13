package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.AuditLogDTO;
import com.crimeLink.analyzer.entity.BackupMetadata;
import com.crimeLink.analyzer.entity.LoginAudit;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.LoginAuditRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import com.crimeLink.analyzer.service.BackupService;
import com.crimeLink.analyzer.service.SystemSettingsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private UserRepository userRepo;
    @Mock private LoginAuditRepository auditRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private BackupService backupService;
    @Mock private SystemSettingsService settingsService;
    @Mock private Authentication authentication;

    @InjectMocks
    private AdminController controller;

    @Test
    void getUsers_shouldReturnAll_whenNoFilters() {
        when(userRepo.findAll()).thenReturn(List.of(new User(), new User()));

        ResponseEntity<List<User>> response = controller.getUsers(null, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void getUsers_shouldReturnByRoleAndStatus() {
        when(userRepo.findByRoleAndStatus("Admin", "Active")).thenReturn(List.of(new User()));

        ResponseEntity<List<User>> response = controller.getUsers("Admin", "Active");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void createUser_shouldReturn400_whenEmailExists() {
        User user = new User();
        user.setEmail("a@test.com");

        when(userRepo.existsByEmail("a@test.com")).thenReturn(true);

        ResponseEntity<?> response = controller.createUser(user);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void createUser_shouldEncodePasswordAndSave() {
        User user = new User();
        user.setEmail("a@test.com");
        user.setPasswordHash("plain");

        when(userRepo.existsByEmail("a@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.createUser(user);

        assertEquals(200, response.getStatusCode().value());
        User saved = (User) response.getBody();
        assertEquals("hashed", saved.getPasswordHash());
    }

    @Test
    void updateUser_shouldReturn404_whenMissing() {
        when(userRepo.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.updateUser(1, new User());

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void updateUser_shouldUpdateAndEncodePassword() {
        User existing = new User();
        existing.setUserId(1);

        User updated = new User();
        updated.setName("New");
        updated.setEmail("new@test.com");
        updated.setPasswordHash("pw");

        when(userRepo.findById(1)).thenReturn(Optional.of(existing));
        when(passwordEncoder.encode("pw")).thenReturn("hashed");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<?> response = controller.updateUser(1, updated);

        assertEquals(200, response.getStatusCode().value());
        User body = (User) response.getBody();
        assertEquals("New", body.getName());
        assertEquals("hashed", body.getPasswordHash());
    }

    @Test
    void deactivateUser_shouldReturn404_whenMissing() {
        when(userRepo.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.deactivateUser(1);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void deactivateUser_shouldSetInactive() {
        User user = new User();
        user.setUserId(1);
        user.setStatus("Active");

        when(userRepo.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = controller.deactivateUser(1);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Inactive", user.getStatus());
        verify(userRepo).save(user);
    }

    @Test
    void getAuditLogs_shouldMapDtos() {
        LoginAudit log = new LoginAudit();
        log.setAuditId(10L);
        log.setUserId(1);
        log.setEmail("u@test.com");
        log.setIpAddress("127.0.0.1");
        log.setLoginTime(LocalDateTime.now());
        log.setSuccess(false);
        log.setFailureReason("Invalid password");

        User user = new User();
        user.setUserId(1);
        user.setName("Officer A");

        Page<LoginAudit> page = new PageImpl<>(List.of(log));
        when(auditRepo.findAll(any(PageRequest.class))).thenReturn(page);
        when(userRepo.findById(1)).thenReturn(Optional.of(user));

        ResponseEntity<List<AuditLogDTO>> response = controller.getAuditLogs(100, 0);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals("Officer A", response.getBody().get(0).getUserName());
    }

    @Test
    void triggerBackup_shouldReturn200() {
        BackupMetadata metadata = new BackupMetadata();
        metadata.setFilename("backup.zip");
        metadata.setSizeBytes(100L);
        metadata.setCreatedAt(LocalDateTime.now());

        when(authentication.getName()).thenReturn("admin@test.com");
        when(backupService.createBackup("admin@test.com")).thenReturn(metadata);

        ResponseEntity<?> response = controller.triggerBackup(authentication);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void restoreBackup_shouldReturn400_whenFilenameMissing() {
        when(authentication.getName()).thenReturn("admin@test.com");

        ResponseEntity<?> response = controller.restoreBackup(Map.of(), authentication);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void restoreBackup_shouldReturn200_whenValid() {
        when(authentication.getName()).thenReturn("admin@test.com");

        ResponseEntity<?> response = controller.restoreBackup(
                Map.of("filename", "backup.zip"), authentication);

        assertEquals(200, response.getStatusCode().value());
        verify(backupService).restoreBackup("backup.zip", "admin@test.com");
    }

    @Test
    void listBackups_shouldReturnList() {
        when(backupService.listBackups()).thenReturn(List.of(new BackupMetadata()));

        ResponseEntity<List<BackupMetadata>> response = controller.listBackups();

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void getSettings_shouldReturnMap() {
        when(settingsService.getAllSettings()).thenReturn(Map.of("theme", "dark"));

        ResponseEntity<Map<String, String>> response = controller.getSettings();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("dark", response.getBody().get("theme"));
    }

    @Test
    void updateSettings_shouldReturn200() {
        when(settingsService.updateSettings(anyMap())).thenReturn(Map.of("theme", "dark"));

        ResponseEntity<?> response = controller.updateSettings(Map.of("theme", "dark"));

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void getSystemHealth_shouldReturnUp() {
        ResponseEntity<?> response = controller.getSystemHealth();
        assertEquals(200, response.getStatusCode().value());
    }
}