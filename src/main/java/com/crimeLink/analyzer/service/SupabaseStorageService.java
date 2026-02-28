package com.crimeLink.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for uploading files to Supabase Storage.
 * Uploads criminal photos to the configured bucket and returns public URLs.
 */
@Service
@Slf4j
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Upload a photo to Supabase Storage.
     *
     * @param criminalId  The criminal's ID (used as folder name)
     * @param file        The image file to upload
     * @return The public URL of the uploaded photo
     */
    public String uploadPhoto(String criminalId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            originalFilename = "photo.jpg";
        }

        // Sanitize filename
        String safeFilename = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String storagePath = criminalId + "/" + safeFilename;

        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + storagePath;

        try {
            byte[] fileBytes = file.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseServiceKey);
            headers.set("apikey", supabaseServiceKey);
            headers.setContentType(MediaType.parseMediaType(
                    file.getContentType() != null ? file.getContentType() : "image/jpeg"));
            // Upsert mode: overwrite if exists
            headers.set("x-upsert", "true");

            HttpEntity<byte[]> request = new HttpEntity<>(fileBytes, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + storagePath;
                log.info("Photo uploaded successfully: {}", publicUrl);
                return publicUrl;
            } else {
                log.error("Supabase upload failed with status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to upload photo to storage. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Failed to upload photo for criminal {}: {}", criminalId, e.getMessage());
            throw new RuntimeException("Photo upload failed: " + e.getMessage(), e);
        }
    }

    /**
     * Delete all files under a criminal's folder in Supabase Storage.
     * Lists files first, then issues a bulk delete.
     *
     * @param criminalId The criminal's ID (used as the folder prefix)
     */
    public void deleteFolder(String criminalId) {
        try {
            // 1) List objects in the folder
            String listUrl = supabaseUrl + "/storage/v1/object/list/" + bucket;

            HttpHeaders listHeaders = new HttpHeaders();
            listHeaders.set("Authorization", "Bearer " + supabaseServiceKey);
            listHeaders.set("apikey", supabaseServiceKey);
            listHeaders.setContentType(MediaType.APPLICATION_JSON);

            String listBody = "{\"prefix\":\"" + criminalId + "/\",\"limit\":100}";
            HttpEntity<String> listRequest = new HttpEntity<>(listBody, listHeaders);

            ResponseEntity<String> listResponse = restTemplate.exchange(
                    listUrl, HttpMethod.POST, listRequest, String.class);

            if (!listResponse.getStatusCode().is2xxSuccessful() || listResponse.getBody() == null) {
                log.warn("Failed to list storage files for criminal {}", criminalId);
                return;
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode files = mapper.readTree(listResponse.getBody());

            if (!files.isArray() || files.isEmpty()) {
                log.info("No storage files found for criminal {}", criminalId);
                return;
            }

            // 2) Collect file paths
            List<String> prefixes = new ArrayList<>();
            for (JsonNode file : files) {
                String fileName = file.has("name") ? file.get("name").asText() : null;
                if (fileName != null) {
                    prefixes.add(criminalId + "/" + fileName);
                }
            }

            if (prefixes.isEmpty()) return;

            // 3) Bulk delete via POST /storage/v1/object/remove
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucket;

            HttpHeaders deleteHeaders = new HttpHeaders();
            deleteHeaders.set("Authorization", "Bearer " + supabaseServiceKey);
            deleteHeaders.set("apikey", supabaseServiceKey);
            deleteHeaders.setContentType(MediaType.APPLICATION_JSON);

            String deleteBody = mapper.writeValueAsString(new java.util.LinkedHashMap<String, Object>() {{
                put("prefixes", prefixes);
            }});
            HttpEntity<String> deleteRequest = new HttpEntity<>(deleteBody, deleteHeaders);

            ResponseEntity<String> deleteResponse = restTemplate.exchange(
                    deleteUrl, HttpMethod.DELETE, deleteRequest, String.class);

            if (deleteResponse.getStatusCode().is2xxSuccessful()) {
                log.info("Storage files deleted for criminal {}: {} file(s)", criminalId, prefixes.size());
            } else {
                log.warn("Storage deletion returned status {} for criminal {}", deleteResponse.getStatusCode(), criminalId);
            }

        } catch (Exception e) {
            log.warn("Storage cleanup failed for criminal {}: {}", criminalId, e.getMessage());
        }
    }
}
