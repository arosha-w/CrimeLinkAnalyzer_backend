package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.IssueWeaponRequestDTO;
import com.crimeLink.analyzer.dto.ReturnWeaponRequestDTO;
import com.crimeLink.analyzer.dto.WeaponAddDTO;

import com.crimeLink.analyzer.service.WeaponIssueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weapon-issue")
@CrossOrigin(origins = "*")
public class WeaponIssueController {

    private final WeaponIssueService weaponIssueService;

    public WeaponIssueController(WeaponIssueService weaponIssueService) {
        this.weaponIssueService = weaponIssueService;
    }

    @GetMapping("/get-active-weapon")
    public List<WeaponAddDTO> getWeapons() {
        return weaponIssueService.getAvailableWeapons();
    }

    @PostMapping("/issue")
    public ResponseEntity<?> issueWeapon(
            @RequestBody IssueWeaponRequestDTO dto
    ) {
        weaponIssueService.issueWeapon(dto);
        return ResponseEntity.ok("Weapon issued");
    }


    @PostMapping("/return")
    public String returnWeapon(@RequestBody ReturnWeaponRequestDTO dto) {
        weaponIssueService.returnWeapon(dto);
        return "Weapon returned";
    }
}
