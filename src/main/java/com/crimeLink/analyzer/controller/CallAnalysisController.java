package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.service.CallAnalysisService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Call Analysis operations.
 * Acts as API Gateway layer, routing requests to the Python ML microservice.
 * 
 * Architecture Pattern: Hybrid Monolith + Microservices
 * - Spring Boot handles authentication, authorization, and request routing
 * - Python FastAPI handles ML inference (call record analysis, NLP)
 * 
 * Endpoints:
 * - POST /api/call-analysis/analyze       - Analyze single call record PDF
 * - POST /api/call-analysis/analyze/batch - Analyze multiple call record PDFs
 * - GET  /api/call-analysis/health        - Check ML service health
 */
@RestController
@RequestMapping("/api/call-analysis")
@RequiredArgsConstructor
@Slf4j
public class CallAnalysisController {

    private final CallAnalysisService callAnalysisService;

    /**
     * Analyze a single call record PDF.
     *
     * @param file PDF file containing call records
     * @return Analysis results with crime indicators, entity graph, etc.
     */
    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeCallRecord(
            @RequestParam("file") MultipartFile file) {
        
        try {
            log.info("Call record analysis requested: {}", file.getOriginalFilename());

            // Validate file
            ResponseEntity<?> validationError = validatePdfFile(file, 10 * 1024 * 1024); // 10MB
            if (validationError != null) {
                return validationError;
            }

            // Forward to ML service
            JsonNode result = callAnalysisService.analyzeCallRecord(file);
            
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("Call record analysis failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Analyze multiple call record PDFs in batch.
     *
     * @param files Array of PDF files containing call records
     * @return Batch analysis results
     */
    @PostMapping("/analyze/batch")
    public ResponseEntity<?> analyzeBatch(
            @RequestParam("files") MultipartFile[] files) {
        
        try {
            log.info("Batch call analysis requested: {} files", files.length);

            // Validate files
            if (files.length == 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No files provided"));
            }

            for (MultipartFile file : files) {
                ResponseEntity<?> validationError = validatePdfFile(file, 10 * 1024 * 1024); // 10MB
                if (validationError != null) {
                    return validationError;
                }
            }

            // Forward to ML service
            List<MultipartFile> fileList = Arrays.asList(files);
            JsonNode result = callAnalysisService.analyzeBatch(fileList);
            
            return ResponseEntity.ok(result);

        } catch (RuntimeException e) {
            log.error("Batch call analysis failed: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint for the call analysis ML service.
     * Public endpoint for monitoring.
     *
     * @return Health status
     */
    @GetMapping("/health")
    public ResponseEntity<?> checkHealth() {
        JsonNode health = callAnalysisService.checkHealth();
        
        String status = health.has("status") ? health.get("status").asText() : "unknown";
        
        if ("healthy".equals(status) || "ok".equals(status)) {
            return ResponseEntity.ok(health);
        } else {
            return ResponseEntity.status(503).body(health);
        }
    }

    /**
     * Validate PDF file for call analysis.
     * Checks: file not empty, content type is PDF, file size within limit.
     *
     * @param file         The file to validate
     * @param maxSizeBytes Maximum allowed file size in bytes
     * @return ResponseEntity with error if validation fails, null if valid
     */
    private ResponseEntity<?> validatePdfFile(MultipartFile file, long maxSizeBytes) {
        if (file == null || file.isEmpty()) {
            log.warn("Validation failed: Empty file");
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "No file provided"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            log.warn("Validation failed: Invalid content type '{}' for file '{}'", 
                    contentType, file.getOriginalFilename());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid file type: " + file.getOriginalFilename() + 
                            ". Only PDF files are allowed."));
        }

        long fileSize = file.getSize();
        if (fileSize > maxSizeBytes) {
            log.warn("Validation failed: File size {} exceeds limit {} for file '{}'", 
                    fileSize, maxSizeBytes, file.getOriginalFilename());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "File size exceeds maximum limit of " + 
                            (maxSizeBytes / (1024 * 1024)) + "MB: " + file.getOriginalFilename()));
        }

        return null; // Validation passed
    }
}
