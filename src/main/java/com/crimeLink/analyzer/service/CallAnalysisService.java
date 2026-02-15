package com.crimeLink.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service for communicating with the Call Analysis ML microservice.
 * Acts as a proxy/gateway layer, forwarding requests from the Spring Boot 
 * monolith to the Python FastAPI microservice.
 * 
 * Architecture: Frontend -> Spring Boot (this service) -> Python ML Service
 */
@Service
@Slf4j
public class CallAnalysisService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${python.call-analysis.url:http://localhost:5001}")
    private String callAnalysisServiceUrl;

    public CallAnalysisService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyze a single call record PDF.
     *
     * @param file PDF file containing call records
     * @return JSON response with analysis results
     */
    public JsonNode analyzeCallRecord(MultipartFile file) {
        log.info("Forwarding call record analysis to ML service: {}", file.getOriginalFilename());
        
        String url = callAnalysisServiceUrl + "/analyze";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Extract file bytes with explicit error handling
            byte[] fileBytes;
            try {
                fileBytes = file.getBytes();
            } catch (IOException e) {
                log.error("Failed to read file bytes from uploaded file '{}': {}", 
                        file.getOriginalFilename(), e.getMessage());
                throw new RuntimeException("Failed to read uploaded file contents", e);
            }
            
            body.add("file", new ByteArrayResource(fileBytes) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("ML service responded with status: {}", response.getStatusCode());
            return objectMapper.readTree(response.getBody());

        } catch (RestClientException e) {
            log.error("Failed to communicate with call analysis service: {}", e.getMessage());
            throw new RuntimeException("Call analysis service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to process response from ML service: {}", e.getMessage());
            throw new RuntimeException("Failed to process ML service response: " + e.getMessage(), e);
        }
    }

    /**
     * Analyze multiple call record PDFs in batch.
     *
     * @param files List of PDF files
     * @return JSON response with batch analysis results
     */
    public JsonNode analyzeBatch(List<MultipartFile> files) {
        log.info("Forwarding batch call analysis to ML service: {} files", files.size());
        
        String url = callAnalysisServiceUrl + "/analyze/batch";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            for (MultipartFile file : files) {
                // Extract file bytes with explicit error handling
                byte[] fileBytes;
                try {
                    fileBytes = file.getBytes();
                } catch (IOException e) {
                    log.error("Failed to read file bytes from uploaded file '{}': {}", 
                            file.getOriginalFilename(), e.getMessage());
                    throw new RuntimeException("Failed to read uploaded file: " + file.getOriginalFilename(), e);
                }
                
                final String originalFilename = file.getOriginalFilename();
                body.add("files", new ByteArrayResource(fileBytes) {
                    @Override
                    public String getFilename() {
                        return originalFilename;
                    }
                });
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Batch analysis completed successfully");
            return objectMapper.readTree(response.getBody());

        } catch (RestClientException e) {
            log.error("Failed to communicate with call analysis service: {}", e.getMessage());
            throw new RuntimeException("Call analysis service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to process files: {}", e.getMessage());
            throw new RuntimeException("Failed to process files: " + e.getMessage(), e);
        }
    }

    /**
     * Check health status of the call analysis ML service.
     *
     * @return Health status JSON
     */
    public JsonNode checkHealth() {
        String url = callAnalysisServiceUrl + "/health";
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (RestClientException e) {
            log.warn("Call analysis service health check failed: {}", e.getMessage());
            return objectMapper.createObjectNode()
                    .put("status", "unhealthy")
                    .put("error", e.getMessage());
        } catch (IOException e) {
            return objectMapper.createObjectNode()
                    .put("status", "unhealthy")
                    .put("error", "Invalid response");
        }
    }
}
