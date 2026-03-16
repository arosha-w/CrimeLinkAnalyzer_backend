package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyRecommendationRequest;
import com.crimeLink.analyzer.dto.OfficerRecommendationDTO;
import com.crimeLink.analyzer.service.DutyRecommendationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DutyRecommendationControllerTest {

    @InjectMocks
    private DutyRecommendationController controller;

    @Mock
    private DutyRecommendationService recommendationService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getRecommendations_shouldReturn200() throws Exception {
        OfficerRecommendationDTO dto = new OfficerRecommendationDTO(
                1, "Officer A", null, 90.0, "Unknown",
                LocalDate.now(), 2, false, "Good fit"
        );

        when(recommendationService.getOfficerRecommendations(any(DutyRecommendationRequest.class)))
                .thenReturn(List.of(dto));

        DutyRecommendationRequest request = new DutyRecommendationRequest();
        request.setDate(LocalDate.now());

        mockMvc.perform(post("/api/duty-recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officerId").value(1))
                .andExpect(jsonPath("$[0].name").value("Officer A"));
    }
}