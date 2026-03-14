package com.crimeLink.analyzer.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallAnalysisServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private CallAnalysisService service;

    @BeforeEach
    void setUp() {
        service = new CallAnalysisService(restTemplate);
        ReflectionTestUtils.setField(service, "callAnalysisServiceUrl", "http://localhost:5001");
    }

    @Test
    void analyzeCallRecord_shouldReturnJson() {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "abc".getBytes());

        when(restTemplate.exchange(
                eq("http://localhost:5001/analyze"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("{\"result\":\"ok\"}"));

        JsonNode result = service.analyzeCallRecord(file);

        assertEquals("ok", result.get("result").asText());
    }

    @Test
    void analyzeCallRecord_shouldThrow_whenRestFails() {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "abc".getBytes());

        when(restTemplate.exchange(
                eq("http://localhost:5001/analyze"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenThrow(new RestClientException("down"));

        assertThrows(RuntimeException.class, () -> service.analyzeCallRecord(file));
    }

    @Test
    void analyzeBatch_shouldReturnJson() {
        MockMultipartFile f1 = new MockMultipartFile("files", "a.pdf", "application/pdf", "abc".getBytes());

        when(restTemplate.exchange(
                eq("http://localhost:5001/analyze/batch"),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(String.class)
        )).thenReturn(ResponseEntity.ok("{\"batch\":\"ok\"}"));

        JsonNode result = service.analyzeBatch(List.of(f1));

        assertEquals("ok", result.get("batch").asText());
    }

    @Test
    void checkHealth_shouldReturnHealthyNode() {
        when(restTemplate.getForEntity("http://localhost:5001/health", String.class))
                .thenReturn(ResponseEntity.ok("{\"status\":\"healthy\"}"));

        JsonNode result = service.checkHealth();

        assertEquals("healthy", result.get("status").asText());
    }

    @Test
    void checkHealth_shouldReturnUnhealthyNode_whenRestFails() {
        when(restTemplate.getForEntity("http://localhost:5001/health", String.class))
                .thenThrow(new RestClientException("down"));

        JsonNode result = service.checkHealth();

        assertEquals("unhealthy", result.get("status").asText());
    }
}