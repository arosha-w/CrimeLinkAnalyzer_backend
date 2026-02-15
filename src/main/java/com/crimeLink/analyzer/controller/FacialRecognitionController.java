package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.service.FacialRecognitionService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * REST Controller for Facial Recognition operations.
 * Acts as API Gateway layer, routing requests to the Python ML microservice.
 * 
 * Architecture Pattern: Hybrid Monolith + Microservices
 * - Spring Boot handles authentication, authorization, and request routing
 * - Python FastAPI handles ML inference (facial recognition)
 * 
 * Endpoints:
 * - POST /api/facial/analyze    - Analyze suspect image for matches
 * - POST /api/facial/register   - Register new criminal face
 * - GET  /api/facial/criminals  - List registered criminals
 * - GET  /api/facial/history    - Get recognition history
 * - GET  /api/facial/health     - Check ML service health
 */
@RestController
@RequestMapping("/api/facial")
@RequiredArgsConstructor
@Slf4j
public class FacialRecognitionController {

    private final FacialRecognitionService facialRecognitionService;

    /**
     * Analyze a suspect image for facial recognition matches.
     * Requires authentication - user ID is extracted from JWT token.
     *
     * @param image     The image file to analyze (multipart)
     * @param threshold Similarity threshold (0-100), default 45
     * @param caseId    Optional case ID for linking to investigation
     * @return Analysis results with matched criminals
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "threshold", required = false, defaultValue = "45") Float threshold,
            @RequestParam(value = "case_id", required = false) String caseId) {
        
        try {
            // Get authenticated user ID from security context
            String userId = getCurrentUserId();
            log.info("Facial recognition analysis requested by user: {}", userId);

            // Validate image
            ResponseEntity<?> validationError = validateImageFile(image, 10 * 1024 * 1024); // 10MB
            if (validationError != null) {
                return validationError;
            }

            // Validate threshold range
            if (threshold != null && (threshold < 0 || threshold > 100)) {
                log.warn("Validation failed: Threshold {} out of range [0-100]", threshold);
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Threshold must be between 0 and 100"));
            }

            // Forward to ML service
            JsonNode result = facialRecognitionService.analyzeImage(image, threshold, userId, caseId);
            
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("Facial recognition analysis failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Register a new criminal with their photo for facial recognition.
     * Requires authentication.
     *
     * @param photo      Photo of the criminal
     * @param criminalId Optional existing criminal ID to link
     * @param name       Criminal's name
     * @param nic        National ID Card number
     * @param riskLevel  Risk level (high, medium, low)
     * @return Registration result with criminal details
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerCriminal(
            @RequestParam("photo") MultipartFile photo,
            @RequestParam(value = "criminal_id", required = false) String criminalId,
            @RequestParam("name") String name,
            @RequestParam("nic") String nic,
            @RequestParam(value = "risk_level", required = false, defaultValue = "medium") String riskLevel) {
        
        try {
            log.info("Criminal registration requested: {} ({})", name, nic);

            // Validate required text fields
            ResponseEntity<?> nameValidation = validateRequiredText(name, "name");
            if (nameValidation != null) {
                return nameValidation;
            }

            ResponseEntity<?> nicValidation = validateRequiredText(nic, "nic");
            if (nicValidation != null) {
                return nicValidation;
            }

            // Validate photo
            ResponseEntity<?> photoValidation = validateImageFile(photo, 10 * 1024 * 1024); // 10MB
            if (photoValidation != null) {
                return photoValidation;
            }

            // Forward to ML service
            JsonNode result = facialRecognitionService.registerCriminal(
                    photo, criminalId, name, nic, riskLevel);
            
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("Criminal registration failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get list of all registered criminals with face embeddings.
     *
     * @return List of criminals
     */
    @GetMapping("/criminals")
    public ResponseEntity<?> getCriminals() {
        try {
            JsonNode result = facialRecognitionService.getCriminals();
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Failed to fetch criminals: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get facial recognition history/audit logs.
     *
     * @param limit Maximum number of records (default 50)
     * @return Recognition history
     */
    @GetMapping("/history")
    public ResponseEntity<?> getRecognitionHistory(
            @RequestParam(value = "limit", required = false, defaultValue = "50") Integer limit) {
        try {
            JsonNode result = facialRecognitionService.getRecognitionHistory(limit);
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            log.error("Failed to fetch history: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint for the facial recognition ML service.
     * Public endpoint for monitoring.
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> checkHealth() {
        JsonNode health = facialRecognitionService.checkHealth();
        
        String status = health.has("status") ? health.get("status").asText() : "unknown";
        
        if ("healthy".equals(status)) {
            return ResponseEntity.ok(health);
        } else {
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Validate image file for facial recognition.
     * Checks: file not empty, content type is image/*, file size within limit.
     *
     * @param file         The file to validate
     * @param maxSizeBytes Maximum allowed file size in bytes
     * @return ResponseEntity with error if validation fails, null if valid
     */
    private ResponseEntity<?> validateImageFile(MultipartFile file, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation failed: Empty image file");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No image provided"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.warn("Validation failed: Invalid content type '{}' for file '{}'", 
                    contentType, file.getOriginalFilename());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file type. Please upload an image."));
        }

        long fileSize = file.getSize();
        if (fileSize > maxSizeBytes) {
            log.warn("Validation failed: Image size {} exceeds limit {} for file '{}'", 
                    fileSize, maxSizeBytes, file.getOriginalFilename());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size exceeds maximum limit of " + 
                            (maxSizeBytes / (1024 * 1024)) + "MB: " + file.getOriginalFilename()));
        }

        return null; // Validation passed
    }

    /**
     * Validate required text field.
     * Checks: not null, not blank after trimming.
     *
     * @param value     The value to validate
     * @param fieldName Name of the field (for error message)
     * @return ResponseEntity with error if validation fails, null if valid
     */
    private ResponseEntity<?> validateRequiredText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            log.warn("Validation failed: Required field '{}' is missing or empty", fieldName);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", fieldName + " is required"));
        }
        return null; // Validation passed
    }

    /**
     * Extract current user ID from security context.
     */
    private String getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "unknown";
    }
}
