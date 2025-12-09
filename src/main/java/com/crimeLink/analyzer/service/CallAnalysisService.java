package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.CallAnalysisResultDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
public class CallAnalysisService {

    @Value("${python.call-analysis.url:http://localhost:5001}")
    private String pythonServiceUrl;

    private final RestTemplate restTemplate;

    public CallAnalysisService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send PDF file to Python service for analysis
     * @param file PDF file
     * @return Analysis ID
     */
    public String analyzeCallRecords(MultipartFile file) throws Exception {
        String url = pythonServiceUrl + "/analyze";

        // Prepare multipart request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Call Python service
        ResponseEntity<Map> response = restTemplate.postForEntity(url, requestEntity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("analysis_id");
        } else {
            throw new Exception("Failed to analyze call records: " + response.getStatusCode());
        }
    }

    /**
     * Get analysis results from Python service
     * @param analysisId Analysis ID
     * @return Analysis results DTO
     */
    public CallAnalysisResultDTO getAnalysisResults(String analysisId) throws Exception {
        String url = pythonServiceUrl + "/results/" + analysisId;

        ResponseEntity<CallAnalysisResultDTO> response = restTemplate.getForEntity(
                url,
                CallAnalysisResultDTO.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
            return null;
        } else {
            throw new Exception("Failed to retrieve analysis results");
        }
    }

    /**
     * Get all analysis history
     * @return List of analyses
     */
    public Map<String, Object> getAllAnalyses() throws Exception {
        String url = pythonServiceUrl + "/results";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new Exception("Failed to retrieve analysis history");
        }
    }

    /**
     * Check if Python service is healthy
     * @return Health status
     */
    public Map<String, Object> checkPythonServiceHealth() throws Exception {
        String url = pythonServiceUrl + "/health";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new Exception("Python service is not responding");
        }
    }
}
