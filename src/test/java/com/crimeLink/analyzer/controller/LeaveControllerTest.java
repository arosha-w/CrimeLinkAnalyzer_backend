package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.LeaveRequestDTO;
import com.crimeLink.analyzer.dto.LeaveSubmitRequest;
import com.crimeLink.analyzer.dto.LeaveUpdateRequest;
import com.crimeLink.analyzer.service.LeaveService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class LeaveControllerTest {

    @InjectMocks
    private LeaveController controller;

    @Mock
    private LeaveService leaveService;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void submitLeaveRequest_shouldReturn200() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(1L);
        dto.setStatus("PENDING");

        when(leaveService.submitLeaveRequest(any(LeaveSubmitRequest.class))).thenReturn(dto);

        LeaveSubmitRequest request = new LeaveSubmitRequest();
        request.setOfficerId(1L);
        request.setDate(LocalDate.of(2026, 3, 13));
        request.setReason("Medical");

        mockMvc.perform(post("/api/leaves/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getOfficerLeaves_shouldReturn200() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(1L);

        when(leaveService.getOfficerLeaves(1L)).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/leaves/officer/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void getAllLeaveRequests_shouldReturn200() throws Exception {
        when(leaveService.getAllLeaveRequests("2026-03")).thenReturn(List.of(new LeaveRequestDTO()));

        mockMvc.perform(get("/api/leaves/all").param("month", "2026-03"))
                .andExpect(status().isOk());
    }

    @Test
    void updateLeaveStatus_shouldUseHeaderUserId_whenProvided() throws Exception {
        LeaveRequestDTO dto = new LeaveRequestDTO();
        dto.setId(1L);
        dto.setStatus("APPROVED");

        when(leaveService.updateLeaveStatus(eq(1L), any(LeaveUpdateRequest.class), eq(5L))).thenReturn(dto);

        LeaveUpdateRequest request = new LeaveUpdateRequest();
        request.setStatus("APPROVED");
        request.setResponseReason("Approved");

        mockMvc.perform(put("/api/leaves/1/status")
                        .header("X-User-Id", "5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }
}