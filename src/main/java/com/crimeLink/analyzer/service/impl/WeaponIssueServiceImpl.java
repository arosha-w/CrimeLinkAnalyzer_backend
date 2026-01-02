package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.IssueWeaponRequestDTO;
import com.crimeLink.analyzer.dto.ReturnWeaponRequestDTO;
import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.entity.WeaponIssue;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.UserRepository;
import com.crimeLink.analyzer.repository.WeaponIssueRepository;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class WeaponIssueServiceImpl implements WeaponIssueService {

    @Autowired
    private WeaponRepository weaponRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WeaponIssueRepository weaponIssueRepository;
    @Override
    public List<WeaponAddDTO> getAllActiveWeapons() {

        List<Weapon> weapons =
                weaponRepository.findByStatus(WeaponStatus.AVAILABLE);

        List<WeaponAddDTO> weaponDTOList = new ArrayList<>();

        for (Weapon weapon : weapons) {

            WeaponAddDTO dto = new WeaponAddDTO();
            dto.setSerialNumber(weapon.getSerialNumber());
            dto.setWeaponType(weapon.getWeaponType());
//            dto.setStatus(weapon.getStatus());
//            dto.setRegisterDate(weapon.getRegisterDate());

            weaponDTOList.add(dto);
        }

        return weaponDTOList;
    }

    @Override
    public List<WeaponAddDTO> getAvailableWeapons() {
        return weaponRepository.findByStatus(WeaponStatus.AVAILABLE)
                .stream()
                .map(weapon -> {
                    WeaponAddDTO dto = new WeaponAddDTO();
                    dto.setSerialNumber(weapon.getSerialNumber());
                    dto.setWeaponType(weapon.getWeaponType());
//                    dto.setStatus(weapon.getStatus());
                    return dto;
                })
                .toList();
    }

    @Override
    public void issueWeapon(IssueWeaponRequestDTO dto) {
        Weapon weapon = weaponRepository.findById(dto.getWeaponSerial())
                .orElseThrow(() -> new RuntimeException("Weapon not found"));

        if (weapon.getStatus().equals(WeaponStatus.ISSUED)) {
            throw new RuntimeException("Weapon already issued");
        }

        WeaponIssue issue = new WeaponIssue();
        issue.setWeaponSerial(dto.getWeaponSerial());
        issue.setOfficerId(dto.getOfficerId());
        issue.setIssuedDate(LocalDate.now());
        issue.setIssuedTime(LocalTime.now());
        issue.setDueDate(dto.getDueDate());
        issue.setIssueNote(dto.getIssueNote());
        issue.setStatus(WeaponStatus.ISSUED);

        weaponIssueRepository.save(issue);

        weapon.setStatus(WeaponStatus.ISSUED);
        weaponRepository.save(weapon);
    }

    @Override
    public void returnWeapon(ReturnWeaponRequestDTO dto) {

        WeaponIssue issue = weaponIssueRepository
                .findByWeapon_SerialNumberAndStatus(dto.getWeaponSerial(), WeaponStatus.ISSUED)
                .orElseThrow(() -> new RuntimeException("Issued record not found"));

        User receivedBy = userRepository.findById(dto.getReceivedByUserId())
                .orElseThrow(() -> new RuntimeException("Receiving officer not found"));

        issue.setReceivedBy(receivedBy);
        issue.setReturnedDate(LocalDate.now());
        issue.setReturnedTime(LocalTime.now());
        issue.setReturnNote(dto.getReturnNote());
        issue.setStatus(WeaponStatus.AVAILABLE);

        weaponIssueRepository.save(issue);

        Weapon weapon = issue.getWeapon();
        weapon.setStatus(WeaponStatus.AVAILABLE);
        weaponRepository.save(weapon);
    }

}
