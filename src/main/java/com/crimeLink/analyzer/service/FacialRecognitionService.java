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
import java.util.Map;

/**
 * Service for communicating with the Facial Recognition ML microservice.
 * Acts as a proxy/gateway layer, forwarding requests from the Spring Boot 
 * monolith to the Python FastAPI microservice.
 * 
 * Architecture: Frontend -> Spring Boot (this service) -> Python ML Service
 */
@Service
@Slf4j
public class FacialRecognitionService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${ml.facial-recognition.url:http://localhost:5002}")
    private String facialRecognitionServiceUrl;

    public FacialRecognitionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyze a suspect image for facial recognition matches.
     * Forwards the request to Python ML service and returns the response.
     *
     * @param image     The image file to analyze
     * @param threshold Similarity threshold (0-100)
     * @param userId    User ID making the request (for audit logging)
     * @param caseId    Optional case ID for linking analysis to investigation
     * @return JSON response from ML service containing matches
     */
    public JsonNode analyzeImage(MultipartFile image, Float threshold, String userId, String caseId) {
        log.info("Forwarding facial recognition request to ML service for user: {}", userId);
        
        String url = facialRecognitionServiceUrl + "/analyze";
        
        try {
            // Build multipart request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Add image file
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename();
                }
            });
            
            // Add optional parameters
            if (threshold != null) {
                body.add("threshold", threshold.toString());
            }
            if (userId != null) {
                body.add("user_id", userId);
            }
            if (caseId != null) {
                body.add("case_id", caseId);
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            log.debug("Sending request to: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("ML service responded with status: {}", response.getStatusCode());
            return objectMapper.readTree(response.getBody());

        } catch (RestClientException e) {
            log.error("Failed to communicate with facial recognition service: {}", e.getMessage());
            throw new RuntimeException("Facial recognition service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to process image file: {}", e.getMessage());
            throw new RuntimeException("Failed to process image: " + e.getMessage(), e);
        }
    }

    /**
     * Register a new criminal with their photo for facial recognition.
     *
     * @param photo       Photo of the criminal
     * @param criminalId  Existing criminal ID to link
     * @param name        Criminal's name
     * @param nic         National ID Card number
     * @param riskLevel   Risk level (high, medium, low)
     * @return JSON response from ML service
     */
    public JsonNode registerCriminal(MultipartFile photo, String criminalId, String name, 
                                      String nic, String riskLevel) {
        log.info("Forwarding criminal registration to ML service: {} ({})", name, nic);
        
        String url = facialRecognitionServiceUrl + "/register";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            body.add("photo", new ByteArrayResource(photo.getBytes()) {
                @Override
                public String getFilename() {
                    return photo.getOriginalFilename();
                }
            });
            
            if (criminalId != null) body.add("criminal_id", criminalId);
            body.add("name", name);
            body.add("nic", nic);
            if (riskLevel != null) body.add("risk_level", riskLevel);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            log.info("Criminal registered successfully");
            return objectMapper.readTree(response.getBody());

        } catch (RestClientException e) {
            log.error("Failed to register criminal: {}", e.getMessage());
            throw new RuntimeException("Facial recognition service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to process photo file: {}", e.getMessage());
            throw new RuntimeException("Failed to process photo: " + e.getMessage(), e);
        }
    }

    /**
     * Get list of all registered criminals with face embeddings.
     *
     * @return JSON array of criminals
     */
    public JsonNode getCriminals() {
        log.debug("Fetching criminals list from ML service");
        
        String url = facialRecognitionServiceUrl + "/criminals";
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (RestClientException e) {
            log.error("Failed to fetch criminals: {}", e.getMessage());
            throw new RuntimeException("Facial recognition service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to parse response: {}", e.getMessage());
            throw new RuntimeException("Invalid response from service: " + e.getMessage(), e);
        }
    }

    /**
     * Get facial recognition history/audit logs.
     *
     * @param limit Maximum number of records
     * @return JSON array of recognition history
     */
    public JsonNode getRecognitionHistory(Integer limit) {
        log.debug("Fetching recognition history from ML service");
        
        String url = facialRecognitionServiceUrl + "/history";
        if (limit != null) {
            url += "?limit=" + limit;
        }
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (RestClientException e) {
            log.error("Failed to fetch history: {}", e.getMessage());
            throw new RuntimeException("Facial recognition service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to parse response: {}", e.getMessage());
            throw new RuntimeException("Invalid response from service: " + e.getMessage(), e);
        }
    }

    /**
     * Check health status of the facial recognition ML service.
     *
     * @return Health status JSON
     */
    public JsonNode checkHealth() {
        String url = facialRecognitionServiceUrl + "/health";
        
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (RestClientException e) {
            log.warn("Facial recognition service health check failed: {}", e.getMessage());
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
