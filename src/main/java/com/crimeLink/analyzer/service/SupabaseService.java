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

@Service
public class SupabaseService {
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String supabaseServiceKey;

    private final RestTemplate restTemplate = new RestTemplate();

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
        return response.getBody();
    }
}
