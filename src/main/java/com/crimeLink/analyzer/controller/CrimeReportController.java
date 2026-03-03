package com.crimeLink.analyzer.controller;

import java.util.List;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.crimeLink.analyzer.dto.CrimeLocationDTO;
import com.crimeLink.analyzer.dto.CrimeReportDTO;
import com.crimeLink.analyzer.service.CrimeReportService;
import com.crimeLink.analyzer.service.SupabaseService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@RequestMapping("/api/crime-reports")
@CrossOrigin("*")
public class CrimeReportController {

    private final CrimeReportService crimeReportService;
    private final SupabaseService supabaseService;

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
    public ResponseEntity<CrimeReportDTO> getCrimeReportById(@PathVariable("id") Long reportId) {
        CrimeReportDTO dto = crimeReportService.getCrimeReportById(reportId);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/map")
    public List<CrimeLocationDTO> getCrimeMapLocations() {
        return crimeReportService.getCrimeMapLocations();
    }

    @PostMapping("/upload-evidence")
    public ResponseEntity<String> uploadEvidence(@RequestParam("file") MultipartFile file) throws Exception {

        String fileUrl = supabaseService.uploadFile(file);
        return ResponseEntity.ok(fileUrl);
    }

    @GetMapping("/download/{reportId}")
    public ResponseEntity<String> downloadEvidence(@PathVariable Long reportId) {
        CrimeReportDTO report = crimeReportService.getCrimeReportById(reportId);

        if (report.getEvidences() == null || report.getEvidences().isEmpty()) {
            return ResponseEntity.badRequest().body("No evidence Found");
        }

        String fileName = report.getEvidences().get(0).getFileName();
        String evidenceUrl = supabaseService.getFileUrl(fileName);
        return ResponseEntity.ok(evidenceUrl);
    }

}
