package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.entity.OfficerPerformance;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.enums.DutyStatus;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import com.crimeLink.analyzer.repository.OfficerPerformanceRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DutyScheduleServiceTest {

    @Mock
    private DutyScheduleRepository dutyRepo;
    @Mock
    private UserRepository userRepo;
    @Mock
    private OfficerPerformanceRepository performanceRepo;

    @InjectMocks
    private DutyScheduleService service;

    @Test
    void getOfficerRowsForDate_shouldReturnEmptyRow_whenNoDutiesForOfficer() {
        User officer = new User();
        officer.setUserId(1);
        officer.setName("Officer A");

        when(userRepo.findByRoleAndStatus("FieldOfficer", "Active")).thenReturn(List.of(officer));
        when(dutyRepo.findByDate(LocalDate.of(2026, 3, 13))).thenReturn(List.of());

        List<OfficerDutyRowDTO> rows = service.getOfficerRowsForDate(LocalDate.of(2026, 3, 13));

        assertEquals(1, rows.size());
        assertEquals("Officer A", rows.get(0).getOfficerName());
        assertEquals("", rows.get(0).getLocation());
    }

    @Test
    void createDuty_shouldThrow_whenOfficerIdMissing() {
        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setDate(LocalDate.now());
        req.setStatus(DutyStatus.Active);

        assertThrows(IllegalArgumentException.class, () -> service.createDuty(req));
    }

    @Test
    void createDuty_shouldThrow_whenDateMissing() {
        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setOfficerId(1);
        req.setStatus(DutyStatus.Active);

        assertThrows(IllegalArgumentException.class, () -> service.createDuty(req));
    }

    @Test
    void createDuty_shouldThrow_whenStatusMissing() {
        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setOfficerId(1);
        req.setDate(LocalDate.now());

        assertThrows(IllegalArgumentException.class, () -> service.createDuty(req));
    }

    @Test
    void createDuty_shouldThrow_whenOfficerNotFound() {
        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setOfficerId(1);
        req.setDate(LocalDate.now());
        req.setStatus(DutyStatus.Active);

        when(userRepo.findById(1)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.createDuty(req));
    }

    @Test
    void createDuty_shouldThrow_whenNonAbsentAndTimeRangeMissing() {
        User officer = new User();
        officer.setUserId(1);

        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setOfficerId(1);
        req.setDate(LocalDate.now());
        req.setStatus(DutyStatus.Active);

        when(userRepo.findById(1)).thenReturn(Optional.of(officer));

        assertThrows(IllegalArgumentException.class, () -> service.createDuty(req));
    }

    @Test
    void createDuty_shouldCreateAbsentDuty_withoutTimeRange() {
        User officer = new User();
        officer.setUserId(1);

        DutySchedule saved = new DutySchedule();
        saved.setAssignedOfficer(officer);
        saved.setDate(LocalDate.now());
        saved.setStatus(DutyStatus.Absent);

        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setOfficerId(1);
        req.setDate(LocalDate.now());
        req.setStatus(DutyStatus.Absent);
        req.setDescription("Absent");

        when(userRepo.findById(1)).thenReturn(Optional.of(officer));
        when(dutyRepo.findByDateAndAssignedOfficer_UserId(req.getDate(), 1)).thenReturn(Optional.empty());
        when(dutyRepo.save(any(DutySchedule.class))).thenReturn(saved);

        DutySchedule result = service.createDuty(req);

        assertEquals(DutyStatus.Absent, result.getStatus());
        verify(performanceRepo, never()).save(any());
    }

    @Test
    void createDuty_shouldCreateActiveDuty_andUpdatePerformance() {
        User officer = new User();
        officer.setUserId(1);
        officer.setName("Officer A");

        DutySchedule saved = new DutySchedule();
        saved.setAssignedOfficer(officer);
        saved.setDate(LocalDate.now());
        saved.setStatus(DutyStatus.Active);
        saved.setTimeRange("08:00-12:00");
        saved.setLocation("Matara");

        DutyScheduleRequest req = new DutyScheduleRequest();
        req.setOfficerId(1);
        req.setDate(LocalDate.now());
        req.setStatus(DutyStatus.Active);
        req.setTimeRange("08:00-12:00");
        req.setLocation("Matara");

        when(userRepo.findById(1)).thenReturn(Optional.of(officer));
        when(dutyRepo.findByDateAndAssignedOfficer_UserIdAndTimeRange(req.getDate(), 1, "08:00-12:00"))
                .thenReturn(Optional.empty());
        when(dutyRepo.save(any(DutySchedule.class))).thenReturn(saved);
        when(performanceRepo.findByOfficer_UserId(1)).thenReturn(List.of());

        DutySchedule result = service.createDuty(req);

        assertEquals(DutyStatus.Active, result.getStatus());
        verify(performanceRepo).save(any(OfficerPerformance.class));
    }

    @Test
    void updateDutyStatus_shouldUpdateExistingDuty() {
        DutySchedule duty = new DutySchedule();
        duty.setId(10L);
        duty.setStatus(DutyStatus.Active);

        when(dutyRepo.findById(10L)).thenReturn(Optional.of(duty));

        service.updateDutyStatus(10L, DutyStatus.Completed);

        assertEquals(DutyStatus.Completed, duty.getStatus());
        verify(dutyRepo).save(duty);
    }

    @Test
    void getDutyLocations_shouldReturnDefault_whenDatabaseEmpty() {
        when(dutyRepo.findDistinctLocations()).thenReturn(List.of());

        List<String> result = service.getDutyLocations();

        assertFalse(result.isEmpty());
        assertTrue(result.contains("Matara"));
    }

    @Test
    void getDutiesBetween_shouldReturnRepositoryData() {
        when(dutyRepo.findByDateBetween(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(List.of(new DutySchedule()));

        List<DutySchedule> result = service.getDutiesBetween(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertEquals(1, result.size());
    }

    @Test
    void generateDutyScheduleReportPdf_shouldReturnBytes() {
        User officer = new User();
        officer.setName("Officer A");

        DutySchedule duty = new DutySchedule();
        duty.setDate(LocalDate.of(2026, 3, 13));
        duty.setAssignedOfficer(officer);
        duty.setLocation("Matara");
        duty.setTimeRange("08:00-12:00");
        duty.setStatus(DutyStatus.Active);
        duty.setDescription("Morning shift");

        when(dutyRepo.findByDateBetween(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31)))
                .thenReturn(List.of(duty));

        byte[] pdf = service.generateDutyScheduleReportPdf(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}