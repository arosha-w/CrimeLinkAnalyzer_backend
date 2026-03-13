package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyAssignmentDTO;
import com.crimeLink.analyzer.dto.DutyDetailDTO;
import com.crimeLink.analyzer.service.MobileDutyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MobileDutyControllerTest {

    @InjectMocks
    private MobileDutyController controller;

    @Mock
    private MobileDutyService mobileDutyService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getDutiesByOfficer_shouldReturn200() throws Exception {
        DutyAssignmentDTO dto = new DutyAssignmentDTO(
                1L, "1", "Officer A", "2026-03-13",
                "Matara", "08:00-12:00", "ACTIVE", "General", "Desc", 0
        );

        when(mobileDutyService.getDutiesByOfficerId(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/duties/officer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officerName").value("Officer A"));
    }

    @Test
    void getDutiesByOfficer_shouldReturn500_whenServiceFails() throws Exception {
        when(mobileDutyService.getDutiesByOfficerId(1L)).thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/duties/officer/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    void getDutyDetailsByDate_shouldReturn200() throws Exception {
        DutyDetailDTO dto = new DutyDetailDTO(
                1L, "General", "Matara", "08:00-12:00", "ACTIVE", "N/A", 0, "Desc"
        );

        when(mobileDutyService.getDutyDetailsByDate(1L, java.time.LocalDate.of(2026, 3, 13)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/duties/officer/1/date").param("date", "2026-03-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].location").value("Matara"));
    }

    @Test
    void getDutyDetailsByDate_shouldReturn500_whenServiceFails() throws Exception {
        when(mobileDutyService.getDutyDetailsByDate(1L, java.time.LocalDate.of(2026, 3, 13)))
                .thenThrow(new RuntimeException("error"));

        mockMvc.perform(get("/api/duties/officer/1/date").param("date", "2026-03-13"))
                .andExpect(status().isInternalServerError());
    }
}