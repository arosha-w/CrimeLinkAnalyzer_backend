package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.service.CallAnalysisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CallAnalysisControllerTest {

    @Mock
    private CallAnalysisService callAnalysisService;

    @InjectMocks
    private CallAnalysisController controller;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void analyzeCallRecord_shouldReturn400_whenNoFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "", "application/pdf", new byte[0]);

        mockMvc.perform(multipart("/api/call-analysis/analyze").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeCallRecord_shouldReturn400_whenWrongType() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.txt", "text/plain", "abc".getBytes());

        mockMvc.perform(multipart("/api/call-analysis/analyze").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeCallRecord_shouldReturn200_whenValid() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.pdf", "application/pdf", "abc".getBytes());
        ObjectNode result = mapper.createObjectNode().put("status", "done");

        when(callAnalysisService.analyzeCallRecord(any())).thenReturn(result);

        mockMvc.perform(multipart("/api/call-analysis/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("done"));
    }

    @Test
    void analyzeBatch_shouldReturn400_whenEmpty() throws Exception {
        mockMvc.perform(multipart("/api/call-analysis/analyze/batch"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void analyzeBatch_shouldReturn200_whenValid() throws Exception {
        MockMultipartFile f1 = new MockMultipartFile("files", "a.pdf", "application/pdf", "abc".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("files", "b.pdf", "application/pdf", "def".getBytes());
        ObjectNode result = mapper.createObjectNode().put("batch", "ok");

        when(callAnalysisService.analyzeBatch(anyList())).thenReturn(result);

        mockMvc.perform(multipart("/api/call-analysis/analyze/batch").file(f1).file(f2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batch").value("ok"));
    }

    @Test
    void health_shouldReturn200_whenHealthy() throws Exception {
        ObjectNode node = mapper.createObjectNode().put("status", "healthy");
        when(callAnalysisService.checkHealth()).thenReturn(node);

        mockMvc.perform(get("/api/call-analysis/health"))
                .andExpect(status().isOk());
    }

    @Test
    void health_shouldReturn503_whenUnhealthy() throws Exception {
        ObjectNode node = mapper.createObjectNode().put("status", "down");
        when(callAnalysisService.checkHealth()).thenReturn(node);

        mockMvc.perform(get("/api/call-analysis/health"))
                .andExpect(status().isServiceUnavailable());
    }
}