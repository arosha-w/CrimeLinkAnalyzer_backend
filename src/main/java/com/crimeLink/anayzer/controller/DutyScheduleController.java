package com.crimeLink.anayzer.controller;

import com.crimeLink.anayzer.dto.DutyScheduleRequest;
import com.crimeLink.anayzer.entity.DutySchedule;
import com.crimeLink.anayzer.service.DutyScheduleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/duty")
public class DutyScheduleController {

    private final DutyScheduleService service;

    public DutyScheduleController(DutyScheduleService service) {
        this.service = service;
    }

    @PostMapping("/save")
    public DutySchedule save(@RequestBody DutyScheduleRequest req) {
        return service.saveSchedule(req);
    }
}
