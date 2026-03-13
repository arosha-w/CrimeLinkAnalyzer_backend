package com.crimeLink.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacialRecognitionServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private FacialRecognitionService service;

    @BeforeEach
    void setUp() {
        service = new FacialRecognitionService(restTemplate);
        ReflectionTestUtils.setField(service, "facialRecognitionServiceUrl", "http://localhost:5002");
    }

    @Test
    void analyzeImage_shouldReturnJson() {
        MockMultipartFile image = new MockMultipartFile("image", "a.jpg", "image/jpeg", "abc".getBytes());

        when(restTemplate.exchange(
                eq("http://localhost:5002/analyze"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("{\"result\":\"ok\"}"));

        JsonNode result = service.analyzeImage(image, 45f, "user-1", null);

        assertEquals("ok", result.get("result").asText());
    }

    @Test
    void registerCriminal_shouldReturnJson() {
        MockMultipartFile photo = new MockMultipartFile("photo", "a.jpg", "image/jpeg", "abc".getBytes());

        when(restTemplate.exchange(
                eq("http://localhost:5002/register"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("{\"saved\":\"yes\"}"));

        JsonNode result = service.registerCriminal(
                photo, null, "John", "123", "medium",
                null, null, null, null, null, null, null, "active"
        );

        assertEquals("yes", result.get("saved").asText());
    }

    @Test
    void getCriminals_shouldReturnJson() {
        when(restTemplate.getForEntity("http://localhost:5002/criminals", String.class))
                .thenReturn(ResponseEntity.ok("{\"count\":1}"));

        JsonNode result = service.getCriminals();

        assertEquals(1, result.get("count").asInt());
    }

    @Test
    void getRecognitionHistory_shouldReturnJson() {
        when(restTemplate.getForEntity("http://localhost:5002/history?limit=10", String.class))
                .thenReturn(ResponseEntity.ok("{\"history\":\"ok\"}"));

        JsonNode result = service.getRecognitionHistory(10);

        assertEquals("ok", result.get("history").asText());
    }

    @Test
    void generateEmbedding_shouldReturnJson() {
        MockMultipartFile photo = new MockMultipartFile("photo", "a.jpg", "image/jpeg", "abc".getBytes());

        when(restTemplate.exchange(
                eq("http://localhost:5002/generate-embedding"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("{\"embedding\":\"done\"}"));

        JsonNode result = service.generateEmbedding("c1", photo);

        assertEquals("done", result.get("embedding").asText());
    }

    @Test
    void checkHealth_shouldReturnUnhealthy_whenRestFails() {
        when(restTemplate.getForEntity("http://localhost:5002/health", String.class))
                .thenThrow(new RestClientException("down"));

        JsonNode result = service.checkHealth();

        assertEquals("unhealthy", result.get("status").asText());
    }
}