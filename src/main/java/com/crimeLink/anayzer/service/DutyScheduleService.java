package com.crimeLink.anayzer.service;

import com.crimeLink.anayzer.dto.DutyScheduleRequest;
import com.crimeLink.anayzer.dto.OfficerDutyRowDTO;
import com.crimeLink.anayzer.entity.DutySchedule;
import com.crimeLink.anayzer.entity.User;
import com.crimeLink.anayzer.repository.DutyScheduleRepository;
import com.crimeLink.anayzer.repository.UserRepository;
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

        List<DutySchedule> dutiesForDate =
                dutyRepo.findByDate(date); // NEW CALL

        List<OfficerDutyRowDTO> rows = new ArrayList<>();

        for (User officer : officers) {

            List<DutySchedule> officerDuties =
                    dutiesForDate.stream()
                            .filter(d -> d.getAssignedOfficer().getUserId().equals(officer.getUserId()))
                            .toList();

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
                            duty.getTimeRange(),
                            duty.getStatus(),
                            duty.getDescription()
                    ));
                }
            }
        }

        return rows;
    }

    public DutySchedule saveDuty(DutyScheduleRequest req) {

        if (req.getOfficerId() == null) {
            throw new IllegalArgumentException("OfficerId is required");
        }
        if (req.getDate() == null) {
            throw new IllegalArgumentException("Date is required");
        }

        User officer = userRepo.findById(req.getOfficerId())
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        DutySchedule duty = new DutySchedule();
        duty.setAssignedOfficer(officer);

        // ✅ SET DATE
        duty.setDate(req.getDate());

        //duty.setDuration(req.getDuration() != null ? req.getDuration() : 240);
        //duty.setTaskType(req.getTaskType() != null ? req.getTaskType() : "General");
        duty.setStatus(req.getStatus() != null ? req.getStatus() : "Active");
        duty.setLocation(req.getLocation());
        duty.setDescription(req.getDescription());
        duty.setTimeRange(req.getTimeRange());

        return dutyRepo.save(duty);
    }


    public List<DutySchedule> getDutiesBetween(LocalDate start, LocalDate end) {
        return dutyRepo.findByDateBetween(start, end);
    }
}
