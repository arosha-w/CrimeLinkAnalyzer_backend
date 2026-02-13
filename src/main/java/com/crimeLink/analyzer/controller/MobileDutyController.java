package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.DutyAssignmentDTO;
import com.crimeLink.analyzer.dto.DutyDetailDTO;
import com.crimeLink.analyzer.service.MobileDutyService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/duties")
@RequiredArgsConstructor
@CrossOrigin(
        origins = "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS},
        allowedHeaders = "*",
        maxAge = 3600
)
public class MobileDutyController {

    private final MobileDutyService mobileDutyService;

    /**
     * Get all duties for a specific officer
     */
    @GetMapping("/officer/{officerId}")
    public ResponseEntity<List<DutyAssignmentDTO>> getDutiesByOfficer(
            @PathVariable Long officerId) {

        System.out.println(" Mobile API Request: GET /officer/" + officerId);

        try {
            List<DutyAssignmentDTO> duties = mobileDutyService.getDutiesByOfficerId(officerId);
            System.out.println(" Returned " + duties.size() + " duties");
            return ResponseEntity.ok(duties);
        } catch (Exception e) {
            System.err.println(" Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * GET /api/duties/officer/{officerId}/date?date=2023-10-24
     * Get duty details for a specific officer on a specific date
     */
    @GetMapping("/officer/{officerId}/date")
    public ResponseEntity<List<DutyDetailDTO>> getDutyDetailsByDate(
            @PathVariable Long officerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        System.out.println(" Mobile API Request: GET /officer/" + officerId + "/date?date=" + date);

        try {
            List<DutyDetailDTO> details = mobileDutyService.getDutyDetailsByDate(officerId, date);
            System.out.println("Returned " + details.size() + " duty details");
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            System.err.println(" Error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}