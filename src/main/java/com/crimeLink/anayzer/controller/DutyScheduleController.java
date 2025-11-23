package com.crimeLink.anayzer.controller;

import com.crimeLink.anayzer.dto.DutyScheduleRequest;
import com.crimeLink.anayzer.dto.OfficerDutyRowDTO;
import com.crimeLink.anayzer.entity.DutySchedule;
import com.crimeLink.anayzer.service.DutyScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/duty-schedules")
@CrossOrigin(origins = "*")
public class DutyScheduleController {

    private final DutyScheduleService dutyService;

    public DutyScheduleController(DutyScheduleService dutyService) {
        this.dutyService = dutyService;
    }

    // ✅ 1) Calendar date click -> officers rows auto-load
    // Example: GET /api/duty-schedules/officers?date=2025-11-25
    @GetMapping("/officers")
    public List<OfficerDutyRowDTO> getOfficersForDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return dutyService.getOfficerRowsForDate(date);
    }

    // ✅ 2) Save duty schedule
    @PostMapping
    public DutySchedule createDuty(@RequestBody DutyScheduleRequest request) {
        return dutyService.saveDuty(request);
    }

    // ✅ 3) Get duties for calendar event range
    // Example: GET /api/duty-schedules/range?start=2025-11-01&end=2025-11-30
    @GetMapping("/range")
    public List<DutySchedule> getDutiesInRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ){
        return dutyService.getDutiesBetween(start, end);
    }
}

