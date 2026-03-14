package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.entity.Vehicle;
import com.crimeLink.analyzer.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService service;

    @Test
    void getAllVehicles_shouldReturnList() {
        when(vehicleRepository.findAll()).thenReturn(List.of(new Vehicle(), new Vehicle()));
        assertEquals(2, service.getAllVehicles().size());
    }

    @Test
    void addVehicle_shouldSetLostDate_whenMissing() {
        Vehicle v = new Vehicle();
        v.setNumberPlate("CAB-1234");
        v.setOwnerName("Owner");
        v.setVehicleType("Car");

        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = service.addVehicle(v);

        assertNotNull(result.getLostDate());
    }

    @Test
    void getVehicleById_shouldReturnVehicle_orNull() {
        Vehicle v = new Vehicle();
        v.setId(1L);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(v));
        when(vehicleRepository.findById(2L)).thenReturn(Optional.empty());

        assertNotNull(service.getVehicleById(1L));
        assertNull(service.getVehicleById(2L));
    }

    @Test
    void deleteVehicle_shouldDelegate() {
        service.deleteVehicle(1L);
        verify(vehicleRepository).deleteById(1L);
    }

    @Test
    void updateVehicle_shouldReturnUpdated_whenFound() {
        Vehicle existing = new Vehicle(1L, "OLD", "Owner", "Car", "Lost", "2026-03-01");
        Vehicle updates = new Vehicle(null, "NEW", "New Owner", "Van", "Found", "2026-03-02");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

        Vehicle result = service.updateVehicle(1L, updates);

        assertNotNull(result);
        assertEquals("NEW", result.getNumberPlate());
        assertEquals("New Owner", result.getOwnerName());
    }

    @Test
    void updateVehicle_shouldReturnNull_whenMissing() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.empty());
        assertNull(service.updateVehicle(1L, new Vehicle()));
    }

    @Test
    void generatePlateRegistryReportPdf_shouldReturnBytes_withoutFilter() {
        when(vehicleRepository.findAll()).thenReturn(List.of(
                new Vehicle(1L, "A", "O1", "Car", "Lost", "2026-03-01")
        ));

        byte[] pdf = service.generatePlateRegistryReportPdf(null, null);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generatePlateRegistryReportPdf_shouldReturnBytes_withFilter() {
        when(vehicleRepository.findAll()).thenReturn(List.of(
                new Vehicle(1L, "A", "O1", "Car", "Lost", "2026-03-10"),
                new Vehicle(2L, "B", "O2", "Van", "Lost", "2026-04-10")
        ));

        byte[] pdf = service.generatePlateRegistryReportPdf("2026-03-01", "2026-03-31");

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}