package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyScheduleRequest;
import com.crimeLink.analyzer.dto.OfficerDutyRowDTO;
import com.crimeLink.analyzer.entity.DutySchedule;
import com.crimeLink.analyzer.service.DutyScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    // 1) Get officer rows for a specific date
    @GetMapping("/officers")
    public ResponseEntity<List<OfficerDutyRowDTO>> getOfficersForDate(
            @RequestParam("date")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        List<OfficerDutyRowDTO> rows = dutyService.getOfficerRowsForDate(date);
        return ResponseEntity.ok(rows);
    }
    // 2) Create / Save a duty (upsert via service.saveDuty)
    @PostMapping
    public ResponseEntity<?> createDuty(@RequestBody DutyScheduleRequest request) {
        try {
            DutySchedule saved = dutyService.createDuty(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (IllegalArgumentException ex) {
            // for validation errors like "Status is required" etc.
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage()); // 400 BAD REQUEST
        } catch (Exception ex) {
            // unexpected server error
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to save duty");
        }
    }
    // 3) Get duties between a date range
    @GetMapping("/range")
    public ResponseEntity<List<DutySchedule>> getDutiesInRange(
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate start,
            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate end
    ) {
        List<DutySchedule> duties = dutyService.getDutiesBetween(start, end);
        return ResponseEntity.ok(duties);
    }
    // 4) Generate Duty Schedule PDF
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
