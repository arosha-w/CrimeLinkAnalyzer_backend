package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.ReportDataDTO;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.enums.DutyStatus;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class ReportController {

    private final UserRepository userRepo;
    private final LoginAuditRepository auditRepo;
    private final DutyScheduleRepository dutyRepo;
    private final WeaponRepository weaponRepo;

    /**
     * Get user activity report
     * GET /api/admin/reports/users?dateFrom=2024-01-01&dateTo=2024-12-31
     */
    @GetMapping("/users")
    public ResponseEntity<ReportDataDTO> getUserActivityReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        ReportDataDTO report = new ReportDataDTO();
        report.setReportType("User Activity Report");
        report.setGeneratedAt(LocalDateTime.now());
        report.setDateFrom(dateFrom);
        report.setDateTo(dateTo);

        // Get all users
        List<Map<String, Object>> userData = new ArrayList<>();
        userRepo.findAll().forEach(user -> {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("role", user.getRole());
            userInfo.put("status", user.getStatus());
            userInfo.put("badgeNo", user.getBadgeNo());
            
            // Count login attempts
            long loginCount = auditRepo.countByUserIdAndSuccessTrue(user.getUserId());
            userInfo.put("loginCount", loginCount);
            
            userData.add(userInfo);
        });

        report.setData(userData);
        
        // Summary stats
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", userRepo.count());
        summary.put("activeUsers", userRepo.countByStatus("Active"));
        summary.put("inactiveUsers", userRepo.countByStatus("Inactive"));
        summary.put("totalLogins", auditRepo.countBySuccessTrue());
        
        report.setSummary(summary);

        return ResponseEntity.ok(report);
    }

    /**
     * Get audit logs report
     * GET /api/admin/reports/audit?dateFrom=2024-01-01&dateTo=2024-12-31
     */
    @GetMapping("/audit")
    public ResponseEntity<ReportDataDTO> getAuditReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        ReportDataDTO report = new ReportDataDTO();
        report.setReportType("Audit Logs Report");
        report.setGeneratedAt(LocalDateTime.now());
        report.setDateFrom(dateFrom);
        report.setDateTo(dateTo);

        LocalDateTime startDateTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime endDateTime = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        List<Map<String, Object>> auditData = new ArrayList<>();
        
        // Get audit logs within date range
        auditRepo.findAll().stream()
                .filter(log -> {
                    if (startDateTime != null && log.getLoginTime().isBefore(startDateTime)) {
                        return false;
                    }
                    if (endDateTime != null && log.getLoginTime().isAfter(endDateTime)) {
                        return false;
                    }
                    return true;
                })
                .forEach(log -> {
                    Map<String, Object> auditInfo = new HashMap<>();
                    auditInfo.put("auditId", log.getAuditId());
                    auditInfo.put("userId", log.getUserId());
                    auditInfo.put("email", log.getEmail());
                    auditInfo.put("ipAddress", log.getIpAddress());
                    auditInfo.put("loginTime", log.getLoginTime().toString());
                    auditInfo.put("success", log.getSuccess());
                    auditInfo.put("failureReason", log.getFailureReason());
                    
                    // Get user name
                    if (log.getUserId() != null) {
                        userRepo.findById(log.getUserId()).ifPresent(user -> {
                            auditInfo.put("userName", user.getName());
                        });
                    }
                    
                    auditData.add(auditInfo);
                });

        report.setData(auditData);
        
        // Summary stats
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAttempts", auditData.size());
        summary.put("successfulLogins", auditData.stream().filter(a -> (Boolean) a.get("success")).count());
        summary.put("failedLogins", auditData.stream().filter(a -> !(Boolean) a.get("success")).count());
        
        report.setSummary(summary);

        return ResponseEntity.ok(report);
    }

    /**
     * Get duty schedule report
     * GET /api/admin/reports/duty?dateFrom=2024-01-01&dateTo=2024-12-31
     */
    @GetMapping("/duty")
    public ResponseEntity<ReportDataDTO> getDutyScheduleReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        ReportDataDTO report = new ReportDataDTO();
        report.setReportType("Duty Schedule Report");
        report.setGeneratedAt(LocalDateTime.now());
        report.setDateFrom(dateFrom);
        report.setDateTo(dateTo);

        List<Map<String, Object>> dutyData = new ArrayList<>();
        
        // Get duty schedules within date range
        dutyRepo.findAll().stream()
                .filter(duty -> {
                    if (dateFrom != null && duty.getDate().isBefore(dateFrom)) {
                        return false;
                    }
                    if (dateTo != null && duty.getDate().isAfter(dateTo)) {
                        return false;
                    }
                    return true;
                })
                .forEach(duty -> {
                    Map<String, Object> dutyInfo = new HashMap<>();
                    dutyInfo.put("scheduleId", duty.getScheduleId());
                    dutyInfo.put("date", duty.getDate().toString());
                    dutyInfo.put("timeRange", duty.getTimeRange());
                    dutyInfo.put("location", duty.getLocation());
                    dutyInfo.put("status", duty.getStatus().toString());
                    
                    // Get officer name
                    User officer = duty.getAssignedOfficer();
                    if (officer != null) {
                        dutyInfo.put("officerId", officer.getUserId());
                        dutyInfo.put("officerName", officer.getName());
                        dutyInfo.put("badgeNo", officer.getBadgeNo());
                    }
                    
                    dutyData.add(dutyInfo);
                });
        // Summary stats
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAssignments", dutyData.size());
        summary.put("completedDuties", dutyRepo.countByStatus(DutyStatus.Completed));
        summary.put("activeDuties", dutyRepo.countByStatus(DutyStatus.Active));
        
        // Count by time range
        Map<String, Long> timeRangeCounts = dutyData.stream()
                .filter(d -> d.get("timeRange") != null)
                .collect(Collectors.groupingBy(d -> (String) d.get("timeRange"), Collectors.counting()));
        summary.put("timeRangeDistribution", timeRangeCounts);
        
        report.setSummary(summary);

        return ResponseEntity.ok(report);
    }

    /**
     * Get system usage report
     * GET /api/admin/reports/system?dateFrom=2024-01-01&dateTo=2024-12-31
     */
    @GetMapping("/system")
    public ResponseEntity<ReportDataDTO> getSystemUsageReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        ReportDataDTO report = new ReportDataDTO();
        report.setReportType("System Usage Report");
        report.setGeneratedAt(LocalDateTime.now());
        report.setDateFrom(dateFrom);
        report.setDateTo(dateTo);

        List<Map<String, Object>> systemData = new ArrayList<>();
        
        // Overall system statistics
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("category", "Users");
        userStats.put("total", userRepo.count());
        userStats.put("active", userRepo.countByStatus("Active"));
        userStats.put("inactive", userRepo.countByStatus("Inactive"));
        systemData.add(userStats);
        
        Map<String, Object> dutyStats = new HashMap<>();
        dutyStats.put("category", "Duty Schedules");
        dutyStats.put("total", dutyRepo.count());
        dutyStats.put("active", dutyRepo.countByStatus(DutyStatus.Active));
        dutyStats.put("completed", dutyRepo.countByStatus(DutyStatus.Completed));
        systemData.add(dutyStats);
        
        Map<String, Object> weaponStats = new HashMap<>();
        weaponStats.put("category", "Weapons");
        weaponStats.put("total", weaponRepo.count());
        weaponStats.put("available", weaponRepo.countByStatus(WeaponStatus.AVAILABLE));
        weaponStats.put("issued", weaponRepo.countByStatus(WeaponStatus.ISSUED));
        systemData.add(weaponStats);
        
        Map<String, Object> auditStats = new HashMap<>();
        auditStats.put("category", "Login Activity");
        auditStats.put("totalAttempts", auditRepo.count());
        auditStats.put("successful", auditRepo.countBySuccessTrue());
        auditStats.put("failed", auditRepo.countBySuccessFalse());
        systemData.add(auditStats);

        report.setData(systemData);
        
        // Summary
        Map<String, Object> summary = new HashMap<>();
        summary.put("reportGeneratedAt", LocalDateTime.now().toString());
        summary.put("systemStatus", "Operational");
        summary.put("databaseStatus", "Connected");
        
        report.setSummary(summary);

        return ResponseEntity.ok(report);
    }

    /**
     * Get weapon usage report
     * GET /api/admin/reports/weapons?dateFrom=2024-01-01&dateTo=2024-12-31
     */
    @GetMapping("/weapons")
    public ResponseEntity<ReportDataDTO> getWeaponReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {

        ReportDataDTO report = new ReportDataDTO();
        report.setReportType("Weapon Usage Report");
        report.setGeneratedAt(LocalDateTime.now());
        report.setDateFrom(dateFrom);
        report.setDateTo(dateTo);

        List<Map<String, Object>> weaponData = new ArrayList<>();
        
        // Get all weapons
        weaponRepo.findAll().forEach(weapon -> {
            Map<String, Object> weaponInfo = new HashMap<>();
            weaponInfo.put("serialNumber", weapon.getSerialNumber());
            weaponInfo.put("weaponType", weapon.getWeaponType());
            weaponInfo.put("status", weapon.getStatus().toString());
            weaponInfo.put("registerDate", weapon.getRegisterDate() != null ? weapon.getRegisterDate().toString() : null);
            weaponInfo.put("updatedDate", weapon.getUpdatedDate() != null ? weapon.getUpdatedDate().toString() : null);
            weaponInfo.put("remarks", weapon.getRemarks());
            
            weaponData.add(weaponInfo);
        });

        report.setData(weaponData);
        
        // Summary stats
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalWeapons", weaponRepo.count());
        summary.put("availableWeapons", weaponRepo.countByStatus(WeaponStatus.AVAILABLE));
        summary.put("issuedWeapons", weaponRepo.countByStatus(WeaponStatus.ISSUED));
        summary.put("maintenanceWeapons", weaponRepo.countByStatus(WeaponStatus.MAINTENANCE));
        
        // Count by type
        Map<String, Long> typeCounts = weaponData.stream()
                .collect(Collectors.groupingBy(w -> (String) w.get("weaponType"), Collectors.counting()));
        summary.put("weaponTypeDistribution", typeCounts);
        
        report.setSummary(summary);

        return ResponseEntity.ok(report);
    }
}
