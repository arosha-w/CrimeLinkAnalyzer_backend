package com.crimeLink.anayzer.controller;

import com.crimeLink.anayzer.dto.CallAnalysisResultDTO;
import com.crimeLink.anayzer.service.CallAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/investigator/call-analysis")
@PreAuthorize("hasAnyRole('Investigator', 'OIC', 'Admin')")
public class CallAnalysisController {

    @Autowired
    private CallAnalysisService callAnalysisService;

    /**
     * Upload PDF file for call record analysis
     * @param file PDF file containing call records
     * @return Analysis ID for tracking results
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadCallRecords(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "No file provided");
                return ResponseEntity.badRequest().body(error);
            }

            if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
                Map<String, Object> error = new HashMap<>();
                error.put("error", "Only PDF files are supported");
                return ResponseEntity.badRequest().body(error);
            }

            // Send to Python service
            String analysisId = callAnalysisService.analyzeCallRecords(file);

            Map<String, Object> response = new HashMap<>();
            response.put("analysis_id", analysisId);
            response.put("status", "processing");
            response.put("message", "Analysis started successfully");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to process file: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get analysis results by ID
     * @param analysisId Analysis ID returned from upload
     * @return Complete analysis results including network graph and criminal matches
     */
    @GetMapping("/results/{analysisId}")
    public ResponseEntity<CallAnalysisResultDTO> getAnalysisResults(@PathVariable String analysisId) {
        try {
            CallAnalysisResultDTO result = callAnalysisService.getAnalysisResults(analysisId);

            if (result == null) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get all analysis history
     * @return List of all analyses
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAnalysisHistory() {
        try {
            Map<String, Object> history = callAnalysisService.getAllAnalyses();
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to retrieve history: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Check Python service health
     * @return Health status of call analysis service
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkServiceHealth() {
        try {
            Map<String, Object> health = callAnalysisService.checkPythonServiceHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "unhealthy");
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
        }
    }
}
