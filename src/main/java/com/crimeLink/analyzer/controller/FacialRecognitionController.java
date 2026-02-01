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
            if (image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No image provided"));
            }

            // Validate file type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type. Please upload an image."));
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

            // Validate photo
            if (photo.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No photo provided"));
            }

            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type. Please upload an image."));
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
