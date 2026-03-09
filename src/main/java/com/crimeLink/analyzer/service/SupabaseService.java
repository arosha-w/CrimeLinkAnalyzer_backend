package com.crimeLink.analyzer.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SupabaseService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String bucket = "crime-evidence";

    public String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException("File size exceeds the limit of 10MB");
        }

        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseServiceKey);
        headers.set("apikey", supabaseServiceKey);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        HttpEntity<byte[]> request = new HttpEntity<>(file.getBytes(), headers);

        restTemplate.exchange(uploadUrl, HttpMethod.PUT, request, String.class);

        return fileName;
    }

    public String getFileUrl(String fileName) {
        String url = supabaseUrl + "/storage/v1/object/sign/" + bucket + "/" + fileName;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseServiceKey);
        headers.set("apikey", supabaseServiceKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"expiresIn\":300}";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        String responseBody = response.getBody();

        if (responseBody == null || responseBody.isBlank()) {
            throw new RuntimeException("Supabase did not return a signed URL");
        }

        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            String signedUrlPath = null;

            if (jsonNode.hasNonNull("signedURL")) {
                signedUrlPath = jsonNode.get("signedURL").asText();
            } else if (jsonNode.hasNonNull("signedUrl")) {
                signedUrlPath = jsonNode.get("signedUrl").asText();
            }

            if (signedUrlPath == null || signedUrlPath.isBlank()) {
                throw new RuntimeException("Supabase signed URL field is missing in response");
            }

            return toAbsoluteSignedUrl(signedUrlPath.trim());
        } catch (IOException exception) {
            throw new RuntimeException("Failed to parse Supabase signed URL response", exception);
        }
    }

    private String toAbsoluteSignedUrl(String signedUrlPath) {
        if (signedUrlPath.startsWith("http://") || signedUrlPath.startsWith("https://")) {
            return signedUrlPath;
        }

        String normalizedSupabaseUrl = supabaseUrl.endsWith("/")
                ? supabaseUrl.substring(0, supabaseUrl.length() - 1)
                : supabaseUrl;

        if (signedUrlPath.startsWith("/storage/v1/")) {
            return normalizedSupabaseUrl + signedUrlPath;
        }

        if (signedUrlPath.startsWith("/")) {
            return normalizedSupabaseUrl + "/storage/v1" + signedUrlPath;
        }

        return normalizedSupabaseUrl + "/storage/v1/" + signedUrlPath;
    }
}
