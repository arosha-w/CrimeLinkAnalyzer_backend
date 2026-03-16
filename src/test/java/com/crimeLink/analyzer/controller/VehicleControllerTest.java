package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.entity.Vehicle;
import com.crimeLink.analyzer.service.VehicleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

    @InjectMocks
    private VehicleController controller;

    @Mock
    private VehicleService vehicleService;

    private MockMvc mockMvc;
    private final ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getVehicles_shouldReturn200() throws Exception {
        when(vehicleService.getAllVehicles()).thenReturn(List.of(new Vehicle()));
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isOk());
    }

    @Test
    void addVehicle_shouldReturn201() throws Exception {
        Vehicle v = new Vehicle();
        v.setId(1L);
        v.setNumberPlate("CAB-1234");

        when(vehicleService.addVehicle(any(Vehicle.class))).thenReturn(v);

        mockMvc.perform(post("/api/vehicles")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(v)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getVehicleById_shouldReturn200_whenFound() throws Exception {
        Vehicle v = new Vehicle();
        v.setId(2L);

        when(vehicleService.getVehicleById(2L)).thenReturn(v);

        mockMvc.perform(get("/api/vehicles/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void getVehicleById_shouldReturn404_whenMissing() throws Exception {
        when(vehicleService.getVehicleById(2L)).thenReturn(null);

        mockMvc.perform(get("/api/vehicles/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteVehicle_shouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/vehicles/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateVehicle_shouldReturn200_whenFound() throws Exception {
        Vehicle v = new Vehicle();
        v.setId(2L);

        when(vehicleService.updateVehicle(anyLong(), any(Vehicle.class))).thenReturn(v);

        mockMvc.perform(put("/api/vehicles/2")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(v)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void updateVehicle_shouldReturn404_whenMissing() throws Exception {
        when(vehicleService.updateVehicle(anyLong(), any(Vehicle.class))).thenReturn(null);

        mockMvc.perform(put("/api/vehicles/2")
                        .contentType(APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new Vehicle())))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPlateRegistryReportPdf_shouldReturnPdf() throws Exception {
        when(vehicleService.generatePlateRegistryReportPdf("2026-03-01", "2026-03-31")).thenReturn("pdf".getBytes());

        mockMvc.perform(get("/api/vehicles/report/pdf")
                        .param("start", "2026-03-01")
                        .param("end", "2026-03-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_PDF));
    }
}