package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.util.LogSanitizer;
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

    @Value("${python.facial-recognition.url:http://localhost:5002}")
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
            
            // Add image file with explicit error handling
            byte[] imageBytes;
            try {
                imageBytes = image.getBytes();
            } catch (IOException e) {
                log.error("Failed to read image bytes from uploaded file '{}': {}", 
                        LogSanitizer.sanitize(image.getOriginalFilename()), e.getMessage());
                throw new RuntimeException("Failed to read uploaded image contents", e);
            }
            
            body.add("image", new ByteArrayResource(imageBytes) {
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
            log.error("Failed to process response from ML service: {}", e.getMessage());
            throw new RuntimeException("Failed to process ML service response: " + e.getMessage(), e);
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
                                      String nic, String riskLevel, String crimeHistory,
                                      String address, String contactNumber, String secondaryContact,
                                      String dateOfBirth, String gender, String alias, String status) {
        log.info("Forwarding criminal registration to ML service: {} ({})", LogSanitizer.sanitize(name), LogSanitizer.sanitize(nic));
        
        String url = facialRecognitionServiceUrl + "/register";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            
            // Extract photo bytes with explicit error handling
            byte[] photoBytes;
            try {
                photoBytes = photo.getBytes();
            } catch (IOException e) {
                log.error("Failed to read photo bytes from uploaded file '{}': {}", 
                        LogSanitizer.sanitize(photo.getOriginalFilename()), e.getMessage());
                throw new RuntimeException("Failed to read uploaded photo contents", e);
            }
            
            body.add("photos", new ByteArrayResource(photoBytes) {
                @Override
                public String getFilename() {
                    return photo.getOriginalFilename();
                }
            });
            
            if (criminalId != null) body.add("criminal_id", criminalId);
            body.add("name", name);
            body.add("nic", nic);
            if (riskLevel != null) body.add("risk_level", riskLevel);
            if (crimeHistory != null) body.add("crime_history", crimeHistory);
            if (address != null) body.add("address", address);
            if (contactNumber != null) body.add("contact_number", contactNumber);
            if (secondaryContact != null) body.add("secondary_contact", secondaryContact);
            if (dateOfBirth != null) body.add("date_of_birth", dateOfBirth);
            if (gender != null) body.add("gender", gender);
            if (alias != null) body.add("alias", alias);
            if (status != null) body.add("status", status);

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
            log.error("Failed to process response from ML service: {}", e.getMessage());
            throw new RuntimeException("Failed to process ML service response: " + e.getMessage(), e);
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
     * Generate a face embedding for an existing criminal by sending the photo
     * to the Python ML service's /generate-embedding endpoint.
     *
     * @param criminalId Existing criminal ID
     * @param photo      Photo file to extract embedding from
     * @return JSON response from ML service
     */
    public JsonNode generateEmbedding(String criminalId, MultipartFile photo) {
        log.info("Requesting embedding generation for criminal: {}", LogSanitizer.sanitize(criminalId));

        String url = facialRecognitionServiceUrl + "/generate-embedding";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            byte[] photoBytes;
            try {
                photoBytes = photo.getBytes();
            } catch (IOException e) {
                log.error("Failed to read photo bytes: {}", e.getMessage());
                throw new RuntimeException("Failed to read uploaded photo", e);
            }

            body.add("photo", new ByteArrayResource(photoBytes) {
                @Override
                public String getFilename() {
                    return photo.getOriginalFilename();
                }
            });
            body.add("criminal_id", criminalId);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, requestEntity, String.class);

            log.info("Embedding generated successfully for criminal {}", LogSanitizer.sanitize(criminalId));
            return objectMapper.readTree(response.getBody());

        } catch (RestClientException e) {
            log.warn("ML service unavailable for embedding generation: {}", e.getMessage());
            throw new RuntimeException("Facial recognition service unavailable: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("Failed to parse ML response: {}", e.getMessage());
            throw new RuntimeException("Failed to process ML service response: " + e.getMessage(), e);
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
