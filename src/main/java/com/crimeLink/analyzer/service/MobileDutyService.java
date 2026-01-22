package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyAssignmentDTO;
import com.crimeLink.analyzer.dto.DutyDetailDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MobileDutyService {

    private final DutyScheduleRepository dutyScheduleRepository;

    @Transactional(readOnly = true)
    public List<DutyAssignmentDTO> getDutiesByOfficerId(Long officerId) {
        List<DutySchedule> duties = dutyScheduleRepository.findByAssignedOfficer_UserId(officerId);

        return duties.stream().map(d -> new DutyAssignmentDTO(
                d.getId(), // ✅ now exists
                String.valueOf(d.getAssignedOfficer().getUserId()),
                d.getAssignedOfficer().getName(),
                d.getDate().toString(),
                d.getLocation(),
                d.getTimeRange(),
                d.getStatus() != null ? d.getStatus().name() : "PENDING",
                "General",
                d.getDescription(),
                0
        )).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DutyDetailDTO> getDutyDetailsByDate(Long officerId, LocalDate date) {
        List<DutySchedule> duties = dutyScheduleRepository.findByDateAndAssignedOfficer_UserId(date, officerId);

        return duties.stream().map(d -> new DutyDetailDTO(
                d.getId(), // ✅ now exists
                "General",
                d.getLocation() != null ? d.getLocation() : "N/A",
                d.getTimeRange() != null ? d.getTimeRange() : "N/A",
                d.getStatus() != null ? d.getStatus().name() : "PENDING",
                "N/A",
                0,
                d.getDescription()
        )).collect(Collectors.toList());
    }
}
