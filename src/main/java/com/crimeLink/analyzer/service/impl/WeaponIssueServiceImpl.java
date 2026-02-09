package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.*;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.entity.WeaponIssue;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.UserRepository;
import com.crimeLink.analyzer.repository.WeaponIssueRepository;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponIssueService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeaponIssueServiceImpl implements WeaponIssueService {

    private final WeaponRepository weaponRepository;
    private final WeaponIssueRepository weaponIssueRepository;
    private final UserRepository userRepository;

    @Override
    public List<WeaponAddDTO> getAllActiveWeapons() {
        List<Weapon> activeWeapons = weaponRepository.findByStatus(WeaponStatus.ISSUED);

        return activeWeapons.stream()
                .map(weapon -> {
                    WeaponAddDTO dto = new WeaponAddDTO();
                    dto.setSerialNumber(weapon.getSerialNumber());
                    dto.setWeaponType(weapon.getWeaponType());
                    dto.setRemarks(weapon.getRemarks());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<WeaponAddDTO> getAvailableWeapons() {
        List<Weapon> availableWeapons = weaponRepository.findByStatus(WeaponStatus.AVAILABLE);

        return availableWeapons.stream()
                .map(weapon -> {
                    WeaponAddDTO dto = new WeaponAddDTO();
                    dto.setSerialNumber(weapon.getSerialNumber());
                    dto.setWeaponType(weapon.getWeaponType());
                    dto.setRemarks(weapon.getRemarks());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void issueWeapon(IssueWeaponRequestDTO dto) {
        // Validate weapon
        Weapon weapon = weaponRepository
                .findBySerialNumber(dto.getWeaponSerial())
                .orElseThrow(() -> new RuntimeException("Weapon not found with serial: " + dto.getWeaponSerial()));

        if (weapon.getStatus() != WeaponStatus.AVAILABLE) {
            throw new RuntimeException("Weapon is not available for issue. Current status: " + weapon.getStatus());
        }

        // Validate users
        User issuedTo = userRepository.findById(dto.getIssuedToId())
                .orElseThrow(() -> new RuntimeException("Issued-to user not found with ID: " + dto.getIssuedToId()));

        User handedOverBy = userRepository.findById(dto.getHandedOverById())
                .orElseThrow(() -> new RuntimeException("Handed-over user not found with ID: " + dto.getHandedOverById()));

        // Create weapon issue
        WeaponIssue issue = new WeaponIssue();
        issue.setWeapon(weapon);
        issue.setIssuedTo(issuedTo);
        issue.setHandedOverBy(handedOverBy);
        issue.setIssuedAt(LocalDateTime.now());
        issue.setDueDate(dto.getDueDate());
        issue.setIssueNote(dto.getIssueNote());
        issue.setStatus(WeaponStatus.ISSUED);

        // ===== ADD BULLET INFORMATION IF PROVIDED =====
        if (dto.getBulletType() != null && !dto.getBulletType().isEmpty()) {
            issue.setBulletType(dto.getBulletType());
            issue.setIssuedMagazines(dto.getNumberOfMagazines());
            issue.setBulletRemarks(dto.getBulletRemarks());
        }

        // Update weapon status
        weapon.setStatus(WeaponStatus.ISSUED);

        // Save
        weaponIssueRepository.save(issue);
        weaponRepository.save(weapon);
    }

    @Override
    @Transactional
    public void returnWeapon(ReturnWeaponRequestDTO dto) {
        // Find weapon
        Weapon weapon = weaponRepository
                .findBySerialNumber(dto.getWeaponSerial())
                .orElseThrow(() -> new RuntimeException("Weapon not found with serial: " + dto.getWeaponSerial()));

        // Find active issue
        WeaponIssue issue = weaponIssueRepository
                .findByWeapon_SerialNumberAndReturnedAtIsNull(weapon.getSerialNumber())
                .orElseThrow(() -> new RuntimeException("No active issue found for this weapon"));

        // Validate receiving user
        User receivedBy = userRepository.findById(dto.getReceivedByUserId())
                .orElseThrow(() -> new RuntimeException("Receiving user not found with ID: " + dto.getReceivedByUserId()));

        // Update return details
        issue.setReturnedAt(LocalDateTime.now());
        issue.setReceivedBy(receivedBy);
        issue.setReturnNote(dto.getReturnNote());

        // ===== UPDATE BULLET RETURN INFORMATION IF PROVIDED =====
        if (dto.getReturnedMagazines() != null) {
            issue.setReturnedMagazines(dto.getReturnedMagazines());
            issue.setUsedBullets(dto.getUsedBullets());
            issue.setBulletCondition(dto.getBulletCondition());
            
            // Append bullet remarks if provided
            if (dto.getBulletRemarks() != null && !dto.getBulletRemarks().isEmpty()) {
                String existingRemarks = issue.getBulletRemarks() != null ? issue.getBulletRemarks() : "";
                issue.setBulletRemarks(existingRemarks + "\n[Return] " + dto.getBulletRemarks());
            }
        }

        // Update weapon status
        weapon.setStatus(WeaponStatus.AVAILABLE);

        // Save
        weaponIssueRepository.save(issue);
        weaponRepository.save(weapon);
    }

    @Override
    public List<OfficerDTO> getAllOfficers() {
        // Get all active users
        List<User> users = userRepository.findByStatus("Active");

        return users.stream()
                .map(user -> {
                    OfficerDTO dto = new OfficerDTO();
                    dto.setId(user.getUserId());
                    dto.setServiceId(user.getBadgeNo());
                    dto.setName(user.getName());
                    dto.setBadge(user.getBadgeNo());
                    dto.setRole(user.getRole());
                    dto.setRank(user.getRole());
                    dto.setStatus(user.getStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}