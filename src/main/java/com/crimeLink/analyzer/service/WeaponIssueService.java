package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.IssueWeaponRequestDTO;
import com.crimeLink.analyzer.dto.OfficerDTO;
import com.crimeLink.analyzer.dto.ReturnWeaponRequestDTO;
import com.crimeLink.analyzer.dto.WeaponAddDTO;

import java.util.List;

public interface WeaponIssueService {
    void issueWeapon(IssueWeaponRequestDTO dto);
    void returnWeapon(ReturnWeaponRequestDTO dto);
    List<WeaponAddDTO> getAllActiveWeapons();
    List<WeaponAddDTO> getAvailableWeapons();
    List<OfficerDTO> getAllOfficers();
}