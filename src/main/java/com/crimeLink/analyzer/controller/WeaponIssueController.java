package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.WeaponAddDTO;

import com.crimeLink.analyzer.service.WeaponIssueService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
