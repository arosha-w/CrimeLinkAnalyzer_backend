package com.crimeLink.analyzer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crimeLink.analyzer.dto.CrimeReportDTO;
import com.crimeLink.analyzer.entity.CrimeReport;
import com.crimeLink.analyzer.service.CrimeReportService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/crime-reports")
@CrossOrigin("*")
public class CrimeReportController {

    private final CrimeReportService crimeReportService;

    @PostMapping
    public ResponseEntity<CrimeReportDTO> saveCrimeReport(@RequestBody CrimeReportDTO crimeReportDTO) {
        CrimeReportDTO report = crimeReportService.saveCrimeReport(crimeReportDTO);
        return new ResponseEntity<>(report, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CrimeReportDTO>> getAllCrimeReports() {
        List<CrimeReportDTO> crimeReports = crimeReportService.getAllCrimeReports();
        return ResponseEntity.ok(crimeReports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CrimeReportDTO> getCrimeReportById(@PathVariable("id") Long reportId){
        CrimeReportDTO dto = crimeReportService.getCrimeReportById(reportId);
        return ResponseEntity.ok(dto);
    }
}
