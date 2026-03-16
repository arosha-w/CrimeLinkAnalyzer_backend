package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyAssignmentDTO;
import com.crimeLink.analyzer.dto.DutyDetailDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.enums.DutyStatus;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class MobileDutyServiceTest {

    @Mock
    private DutyScheduleRepository dutyScheduleRepository;

    @InjectMocks
    private MobileDutyService service;

    @Test
    void getDutiesByOfficerId_shouldReturnMappedAssignments() {
        User officer = new User();
        officer.setUserId(1);
        officer.setName("Officer A");

        DutySchedule duty = new DutySchedule();
        duty.setId(10L);
        duty.setAssignedOfficer(officer);
        duty.setDate(LocalDate.of(2026, 3, 13));
        duty.setLocation("Matara");
        duty.setTimeRange("08:00-12:00");
        duty.setStatus(DutyStatus.Active);
        duty.setDescription("Morning duty");

        when(dutyScheduleRepository.findByAssignedOfficer_UserId(1L)).thenReturn(List.of(duty));

        List<DutyAssignmentDTO> result = service.getDutiesByOfficerId(1L);

        assertEquals(1, result.size());
        assertEquals("Officer A", result.get(0).getOfficerName());
        assertEquals(DutyStatus.Active.name(), result.get(0).getStatus());
    }

    @Test
    void getDutyDetailsByDate_shouldReturnMappedDetails() {
        User officer = new User();
        officer.setUserId(1);
        officer.setName("Officer A");

        DutySchedule duty = new DutySchedule();
        duty.setId(10L);
        duty.setAssignedOfficer(officer);
        duty.setDate(LocalDate.of(2026, 3, 13));
        duty.setLocation(null);
        duty.setTimeRange(null);
        duty.setStatus(null);
        duty.setDescription("Description");

        when(dutyScheduleRepository.findByDateAndAssignedOfficer_UserId(LocalDate.of(2026, 3, 13), 1L))
                .thenReturn(List.of(duty));

        List<DutyDetailDTO> result = service.getDutyDetailsByDate(1L, LocalDate.of(2026, 3, 13));

        assertEquals(1, result.size());
        assertEquals("N/A", result.get(0).getLocation());
        assertEquals("N/A", result.get(0).getTimeRange());
        assertEquals("PENDING", result.get(0).getStatus());
    }
}