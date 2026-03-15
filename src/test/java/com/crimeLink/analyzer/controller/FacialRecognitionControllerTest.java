package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.service.FacialRecognitionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class FacialRecognitionControllerTest {

    @Mock
    private FacialRecognitionService facialRecognitionService;

    @InjectMocks
    private FacialRecognitionController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user-1", null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void analyzeImage_shouldReturn400_whenNoImage() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "", "image/jpeg", new byte[0]);

        mockMvc.perform(multipart("/api/facial/analyze").file(image))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeImage_shouldReturn400_whenWrongType() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "a.txt", "text/plain", "abc".getBytes());

        mockMvc.perform(multipart("/api/facial/analyze").file(image))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeImage_shouldReturn400_whenThresholdInvalid() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "a.jpg", "image/jpeg", "abc".getBytes());

        mockMvc.perform(multipart("/api/facial/analyze")
                        .file(image)
                        .param("threshold", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeImage_shouldReturn200_whenValid() throws Exception {
        MockMultipartFile image = new MockMultipartFile("image", "a.jpg", "image/jpeg", "abc".getBytes());
        ObjectNode result = mapper.createObjectNode().put("match", "ok");

        when(facialRecognitionService.analyzeImage(any(), eq(45.0f), eq("unknown"), isNull()))
                .thenReturn(result);

        mockMvc.perform(multipart("/api/facial/analyze").file(image))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.match").value("ok"));
    }

    @Test
    void registerCriminal_shouldReturn400_whenNameMissing() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photo", "a.jpg", "image/jpeg", "abc".getBytes());

        mockMvc.perform(multipart("/api/facial/register")
                        .file(photo)
                        .param("name", "")
                        .param("nic", "123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCriminal_shouldReturn400_whenNicMissing() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photo", "a.jpg", "image/jpeg", "abc".getBytes());

        mockMvc.perform(multipart("/api/facial/register")
                        .file(photo)
                        .param("name", "John")
                        .param("nic", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCriminal_shouldReturn200_whenValid() throws Exception {
        MockMultipartFile photo = new MockMultipartFile("photo", "a.jpg", "image/jpeg", "abc".getBytes());
        ObjectNode result = mapper.createObjectNode().put("registered", "yes");

        when(facialRecognitionService.registerCriminal(
                any(), any(), eq("John"), eq("123"), any(), any(), any(), any(), any(), any(), any(), any(), any()
        )).thenReturn(result);

        mockMvc.perform(multipart("/api/facial/register")
                        .file(photo)
                        .param("name", "John")
                        .param("nic", "123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.registered").value("yes"));
    }

    @Test
    void getCriminals_shouldReturn200() throws Exception {
        ObjectNode result = mapper.createObjectNode().put("count", 1);
        when(facialRecognitionService.getCriminals()).thenReturn(result);

        mockMvc.perform(get("/api/facial/criminals"))
                .andExpect(status().isOk());
    }

    @Test
    void getRecognitionHistory_shouldReturn200() throws Exception {
        ObjectNode result = mapper.createObjectNode().put("history", "ok");
        when(facialRecognitionService.getRecognitionHistory(50)).thenReturn(result);

        mockMvc.perform(get("/api/facial/history"))
                .andExpect(status().isOk());
    }

    @Test
    void health_shouldReturn200_whenHealthy() throws Exception {
        ObjectNode result = mapper.createObjectNode().put("status", "healthy");
        when(facialRecognitionService.checkHealth()).thenReturn(result);

        mockMvc.perform(get("/api/facial/health"))
                .andExpect(status().isOk());
    }

    @Test
    void health_shouldReturn503_whenUnhealthy() throws Exception {
        ObjectNode result = mapper.createObjectNode().put("status", "down");
        when(facialRecognitionService.checkHealth()).thenReturn(result);

        mockMvc.perform(get("/api/facial/health"))
                .andExpect(status().isServiceUnavailable());
    }
}