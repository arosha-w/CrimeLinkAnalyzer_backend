package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.IssueWeaponRequestDTO;
import com.crimeLink.analyzer.dto.OfficerDTO;
import com.crimeLink.analyzer.dto.ReturnWeaponRequestDTO;
import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.service.WeaponIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weapon")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeaponIssueController {

    private final WeaponIssueService weaponIssueService;

    @PostMapping("/issue")
    public ResponseEntity<?> issueWeapon(@RequestBody IssueWeaponRequestDTO dto) {
        try {
            weaponIssueService.issueWeapon(dto);
            return ResponseEntity.ok(createSuccessResponse("Weapon issued successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to issue weapon"));
        }
    }

    @PostMapping("/return")
    public ResponseEntity<?> returnWeapon(@RequestBody ReturnWeaponRequestDTO dto) {
        try {
            weaponIssueService.returnWeapon(dto);
            return ResponseEntity.ok(createSuccessResponse("Weapon returned successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to return weapon"));
        }
    }

    @GetMapping("/active")
    public ResponseEntity<?> getAllActiveWeapons() {
        try {
            List<WeaponAddDTO> weapons = weaponIssueService.getAllActiveWeapons();
            return ResponseEntity.ok(weapons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch active weapons"));
        }
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableWeapons() {
        try {
            List<WeaponAddDTO> weapons = weaponIssueService.getAvailableWeapons();
            return ResponseEntity.ok(weapons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch available weapons"));
        }
    }

    @GetMapping("/officers")
    public ResponseEntity<?> getAllOfficers() {
        try {
            List<OfficerDTO> officers = weaponIssueService.getAllOfficers();
            return ResponseEntity.ok(officers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch officers"));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }

    private Map<String, String> createSuccessResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        return response;
    }
}