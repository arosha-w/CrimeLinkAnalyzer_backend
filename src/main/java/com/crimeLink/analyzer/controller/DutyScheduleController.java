package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.service.DutyScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

// also needed:
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

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

    @GetMapping("/officers")
    public List<OfficerDutyRowDTO> getOfficersForDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return dutyService.getOfficerRowsForDate(date);
    }

    @PostMapping
    public DutySchedule createDuty(@RequestBody DutyScheduleRequest request) {
        return dutyService.saveDuty(request);
    }


    @GetMapping("/range")
    public List<DutySchedule> getDutiesInRange(
            @RequestParam LocalDate start,
            @RequestParam LocalDate end
    ){
        return dutyService.getDutiesBetween(start, end);
    }
    @GetMapping(value = "/report/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getDutyScheduleReportPdf(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        byte[] pdfBytes = dutyService.generateDutyScheduleReportPdf(start, end);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        // this makes browser download as a file named duty-schedule-report.pdf
        headers.setContentDispositionFormData("attachment", "duty-schedule-report.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}

