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
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No file provided"));
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.equals("application/pdf")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Invalid file type. Please upload a PDF file."));
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
                if (file.isEmpty()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "One or more files are empty"));
                }
                String contentType = file.getContentType();
                if (contentType == null || !contentType.equals("application/pdf")) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid file type: " + file.getOriginalFilename() + ". Only PDF files are allowed."));
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
}
