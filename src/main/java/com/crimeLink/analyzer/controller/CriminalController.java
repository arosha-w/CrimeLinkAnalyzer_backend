package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.service.CriminalService;
import com.crimeLink.analyzer.util.LogSanitizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for Criminal record management (CRUD).
 * Operates directly against the database via JPA — coordinates with Python ML for embeddings.
 *
 * Endpoints:
 * - POST   /api/criminals          - Create new criminal (with photo upload + embedding)
 * - GET    /api/criminals          - List all criminals
 * - GET    /api/criminals/{id}     - Get criminal details
 * - PUT    /api/criminals/{id}     - Update criminal profile (with optional photo)
 * - DELETE /api/criminals/{id}     - Delete criminal record
 */
@RestController
@RequestMapping("/api/criminals")
@RequiredArgsConstructor
@Slf4j
public class CriminalController {

    private final CriminalService criminalService;

    /**
     * Create a new criminal record with optional photo.
     */
    @PostMapping
    public ResponseEntity<?> createCriminal(
            @RequestParam("name") String name,
            @RequestParam("nic") String nic,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "crime_history", required = false) String crimeHistory,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "contact_number", required = false) String contactNumber,
            @RequestParam(value = "secondary_contact", required = false) String secondaryContact,
            @RequestParam(value = "date_of_birth", required = false) String dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "alias", required = false) String alias,
            @RequestParam(value = "status", required = false) String status) {

        try {
            log.info("Criminal registration requested: name={}, nic={}", 
                    LogSanitizer.sanitize(name), LogSanitizer.sanitize(nic));

            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Name is required"));
            }
            if (nic == null || nic.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "NIC is required"));
            }

            Map<String, Object> result = criminalService.createCriminal(
                    name.trim(), nic.trim(), riskLevel, crimeHistory,
                    address, contactNumber, secondaryContact,
                    dateOfBirth, gender, alias, status, photo);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Criminal registration failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Get all criminals (summary list).
     */
    @GetMapping
    public ResponseEntity<?> getAllCriminals() {
        try {
            List<Map<String, Object>> criminals = criminalService.getAllCriminals();
            return ResponseEntity.ok(criminals);
        } catch (Exception e) {
            log.error("Failed to fetch criminals: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch criminals: " + e.getMessage()));
        }
    }

    /**
     * Get full details for a specific criminal.
     */
    @GetMapping("/{criminalId}")
    public ResponseEntity<?> getCriminalDetails(@PathVariable String criminalId) {
        try {
            Optional<Map<String, Object>> result = criminalService.getCriminalDetails(criminalId);
            if (result.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Criminal not found: " + LogSanitizer.sanitize(criminalId)));
            }
            return ResponseEntity.ok(result.get());
        } catch (Exception e) {
            log.error("Failed to fetch criminal {}: {}", LogSanitizer.sanitize(criminalId), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to fetch criminal details: " + e.getMessage()));
        }
    }

    /**
     * Update an existing criminal's profile data (with optional photo change).
     */
    @PutMapping("/{criminalId}")
    public ResponseEntity<?> updateCriminal(
            @PathVariable String criminalId,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "nic", required = false) String nic,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "risk_level", required = false) String riskLevel,
            @RequestParam(value = "crime_history", required = false) String crimeHistory,
            @RequestParam(value = "address", required = false) String address,
            @RequestParam(value = "contact_number", required = false) String contactNumber,
            @RequestParam(value = "secondary_contact", required = false) String secondaryContact,
            @RequestParam(value = "date_of_birth", required = false) String dateOfBirth,
            @RequestParam(value = "gender", required = false) String gender,
            @RequestParam(value = "alias", required = false) String alias,
            @RequestParam(value = "status", required = false) String status) {

        try {
            log.info("Criminal update requested for ID: {}", LogSanitizer.sanitize(criminalId));

            Optional<Map<String, Object>> result = criminalService.updateCriminal(
                    criminalId, name, nic, riskLevel, crimeHistory,
                    address, contactNumber, secondaryContact,
                    dateOfBirth, gender, alias, status, photo);

            if (result.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Criminal not found: " + LogSanitizer.sanitize(criminalId)));
            }

            Map<String, Object> response = new java.util.LinkedHashMap<>();
            response.put("message", "Criminal updated successfully");
            response.put("criminal", result.get());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Criminal update failed for {}: {}", LogSanitizer.sanitize(criminalId), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Update failed: " + e.getMessage()));
        }
    }

    /**
     * Delete a criminal record (cascades to suspect_photos, cleans up storage).
     */
    @DeleteMapping("/{criminalId}")
    public ResponseEntity<?> deleteCriminal(@PathVariable String criminalId) {
        try {
            log.info("Criminal deletion requested for ID: {}", LogSanitizer.sanitize(criminalId));

            boolean deleted = criminalService.deleteCriminal(criminalId);
            if (!deleted) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Criminal not found: " + LogSanitizer.sanitize(criminalId)));
            }

            return ResponseEntity.ok(Map.of("message", "Criminal deleted successfully", "criminal_id", criminalId));

        } catch (Exception e) {
            log.error("Criminal deletion failed for {}: {}", LogSanitizer.sanitize(criminalId), e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }
}
