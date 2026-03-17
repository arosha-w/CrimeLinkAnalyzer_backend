package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.enums.DutyStatus;
import com.crimeLink.analyzer.service.DutyScheduleService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DutyScheduleControllerTest {

    @InjectMocks
    private DutyScheduleController controller;

    @Mock
    private DutyScheduleService dutyService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getOfficersForDate_shouldReturn200() throws Exception {
        OfficerDutyRowDTO dto = new OfficerDutyRowDTO(1, "Officer A", "Matara", "08:00-12:00", "ACTIVE", "Desc");

        when(dutyService.getOfficerRowsForDate(LocalDate.of(2026, 3, 13))).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/duty-schedules/officers")
                        .param("date", "2026-03-13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].officerId").value(1));
    }

    @Test
    void getDutyLocations_shouldReturn200() throws Exception {
        when(dutyService.getDutyLocations()).thenReturn(List.of("Matara", "Hakmana"));

        mockMvc.perform(get("/api/duty-schedules/locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Matara"));
    }

    @Test
    void createDuty_shouldReturn201_whenCreated() throws Exception {
        DutySchedule duty = new DutySchedule();
        duty.setId(1L);

        when(dutyService.createDuty(any(DutyScheduleRequest.class))).thenReturn(duty);

        DutyScheduleRequest request = new DutyScheduleRequest();
        request.setOfficerId(1);
        request.setDate(LocalDate.of(2026, 3, 13));
        request.setStatus(DutyStatus.Active);
        request.setTimeRange("08:00-12:00");

        mockMvc.perform(post("/api/duty-schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createDuty_shouldReturn400_whenValidationError() throws Exception {
        when(dutyService.createDuty(any(DutyScheduleRequest.class)))
                .thenThrow(new IllegalArgumentException("Status is required"));

        DutyScheduleRequest request = new DutyScheduleRequest();

        mockMvc.perform(post("/api/duty-schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Status is required"));
    }

    @Test
    void createDuty_shouldReturn500_whenUnexpectedError() throws Exception {
        when(dutyService.createDuty(any(DutyScheduleRequest.class)))
                .thenThrow(new RuntimeException("DB error"));

        DutyScheduleRequest request = new DutyScheduleRequest();

        mockMvc.perform(post("/api/duty-schedules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Failed to save duty"));
    }

    @Test
    void getDutiesInRange_shouldReturn200() throws Exception {
        when(dutyService.getDutiesBetween(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(List.of(new DutySchedule()));

        mockMvc.perform(get("/api/duty-schedules/range")
                        .param("start", "2026-03-01")
                        .param("end", "2026-03-31"))
                .andExpect(status().isOk());
    }

    @Test
    void getDutyScheduleReportPdf_shouldReturnPdf() throws Exception {
        when(dutyService.generateDutyScheduleReportPdf(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn("pdf-content".getBytes());

        mockMvc.perform(get("/api/duty-schedules/report/pdf")
                        .param("start", "2026-03-01")
                        .param("end", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }

    @Test
    void createDutiesBulk_shouldReturn201() throws Exception {
        DutySchedule duty = new DutySchedule();
        duty.setId(1L);

        when(dutyService.createDuties(any())).thenReturn(List.of(duty));

        DutyScheduleRequest request = new DutyScheduleRequest();
        request.setOfficerId(1);
        request.setDate(LocalDate.of(2026, 3, 13));
        request.setStatus(DutyStatus.Active);
        request.setTimeRange("06:00-21:00");
        request.setLocation("Matara");

        mockMvc.perform(post("/api/duty-schedules/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isCreated());
    }

    @Test
    void createDutiesBulk_shouldReturn400_whenValidationError() throws Exception {
        when(dutyService.createDuties(any()))
                .thenThrow(new IllegalArgumentException("OfficerId is required"));

        DutyScheduleRequest request = new DutyScheduleRequest();

        mockMvc.perform(post("/api/duty-schedules/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(List.of(request))))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("OfficerId is required"));
    }
}