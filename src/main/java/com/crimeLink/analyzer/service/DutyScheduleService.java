package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.repository.DutyScheduleRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DutyScheduleService {

    private final DutyScheduleRepository dutyRepo;
    private final UserRepository userRepo;

    public List<OfficerDutyRowDTO> getOfficerRowsForDate(LocalDate date) {

        List<User> officers = userRepo.findByRoleAndStatus("FieldOfficer", "Active");

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        List<OfficerDutyRowDTO> rows = new ArrayList<>();
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (User officer : officers) {

            List<DutySchedule> officerDuties =
                    dutyRepo.findByAssignedOfficer_UserIdAndDatetimeBetween(
                            officer.getUserId(), start, end
                    );

            if (officerDuties.isEmpty()) {
                rows.add(new OfficerDutyRowDTO(
                        officer.getUserId(),
                        officer.getName(),
                        "", "", "", ""
                ));
            } else {
                for (DutySchedule duty : officerDuties) {
                    rows.add(new OfficerDutyRowDTO(
                            officer.getUserId(),
                            officer.getName(),
                            duty.getLocation(),
                            duty.getDatetime().format(timeFmt),
                            duty.getStatus(),
                            duty.getDescription()
                    ));
                }
            }
        }

        return rows;
    }

    public DutySchedule saveDuty(DutyScheduleRequest req) {

        User officer = userRepo.findById(req.getOfficerId())
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        DutySchedule duty = new DutySchedule();
        duty.setAssignedOfficer(officer);
        duty.setDatetime(req.getDatetime());
        duty.setDuration(req.getDuration());
        duty.setTaskType(req.getTaskType());
        duty.setStatus(req.getStatus());
        duty.setLocation(req.getLocation());
        duty.setDescription(req.getDescription());

        return dutyRepo.save(duty);
    }

    public List<DutySchedule> getDutiesBetween(LocalDate start, LocalDate end) {
        return dutyRepo.findByDatetimeBetween(
                start.atStartOfDay(),
                end.plusDays(1).atStartOfDay()
        );
    }
}
