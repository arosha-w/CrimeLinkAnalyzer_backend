package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.WeaponAddDTO;

import java.util.List;

public interface WeaponIssueService {

    List<WeaponAddDTO> getAllActiveWeapons();



    List<WeaponAddDTO> getAvailableWeapons();
}
