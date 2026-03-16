package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyRecommendationRequest;
import com.crimeLink.analyzer.dto.OfficerRecommendationDTO;
import com.crimeLink.analyzer.entity.OfficerPerformance;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.OfficerPerformanceRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class DutyRecommendationServiceTest {

    @Mock
    private UserRepository userRepo;

    @Mock
    private OfficerPerformanceRepository performanceRepo;

    @InjectMocks
    private DutyRecommendationService service;

    @Test
    void getOfficerRecommendations_shouldReturnRankedList() {
        User officer1 = new User();
        officer1.setUserId(1);
        officer1.setName("Officer A");

        User officer2 = new User();
        officer2.setUserId(2);
        officer2.setName("Officer B");

        OfficerPerformance perf1 = OfficerPerformance.builder()
                .officer(officer1)
                .totalDuties(3)
                .lastDutyDate(LocalDate.now().minusDays(8))
                .reliabilityScore(90)
                .build();

        OfficerPerformance perf2 = OfficerPerformance.builder()
                .officer(officer2)
                .totalDuties(20)
                .lastDutyDate(LocalDate.now().minusDays(1))
                .reliabilityScore(60)
                .build();

        DutyRecommendationRequest req = new DutyRecommendationRequest();
        req.setDate(LocalDate.now());
        req.setRequiredOfficers(2);

        when(userRepo.findByRoleAndStatus("FieldOfficer", "Active"))
                .thenReturn(List.of(officer1, officer2));
        when(performanceRepo.findByOfficer_UserId(1)).thenReturn(List.of(perf1));
        when(performanceRepo.findByOfficer_UserId(2)).thenReturn(List.of(perf2));

        List<OfficerRecommendationDTO> result = service.getOfficerRecommendations(req);

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOfficerId());
        assertTrue(result.get(0).getRecommendationScore() >= result.get(1).getRecommendationScore());
    }

    @Test
    void getOfficerRecommendations_shouldUseDefaultValues_whenNoPerformanceData() {
        User officer = new User();
        officer.setUserId(1);
        officer.setName("Officer A");

        DutyRecommendationRequest req = new DutyRecommendationRequest();
        req.setDate(LocalDate.now());

        when(userRepo.findByRoleAndStatus("FieldOfficer", "Active"))
                .thenReturn(List.of(officer));
        when(performanceRepo.findByOfficer_UserId(1)).thenReturn(List.of());

        List<OfficerRecommendationDTO> result = service.getOfficerRecommendations(req);

        assertEquals(1, result.size());
        assertEquals("Unknown", result.get(0).getAvailabilityStatus());
        assertEquals(0, result.get(0).getTotalDuties());
        assertNotNull(result.get(0).getReason());
    }

    @Test
    void getOfficerRecommendations_shouldLimitResults_whenRequiredOfficerCountProvided() {
        User officer1 = new User(); officer1.setUserId(1); officer1.setName("A");
        User officer2 = new User(); officer2.setUserId(2); officer2.setName("B");
        User officer3 = new User(); officer3.setUserId(3); officer3.setName("C");

        DutyRecommendationRequest req = new DutyRecommendationRequest();
        req.setDate(LocalDate.now());
        req.setRequiredOfficers(2);

        when(userRepo.findByRoleAndStatus("FieldOfficer", "Active"))
                .thenReturn(List.of(officer1, officer2, officer3));
        when(performanceRepo.findByOfficer_UserId(1)).thenReturn(List.of());
        when(performanceRepo.findByOfficer_UserId(2)).thenReturn(List.of());
        when(performanceRepo.findByOfficer_UserId(3)).thenReturn(List.of());

        List<OfficerRecommendationDTO> result = service.getOfficerRecommendations(req);

        assertEquals(2, result.size());
    }
}