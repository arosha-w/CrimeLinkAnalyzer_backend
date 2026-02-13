package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.*;
import com.crimeLink.analyzer.entity.Bullet;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.entity.WeaponIssue;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.BulletRepository;
import com.crimeLink.analyzer.repository.UserRepository;
import com.crimeLink.analyzer.repository.WeaponIssueRepository;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponIssueService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class WeaponIssueServiceImpl implements WeaponIssueService {

    private final WeaponRepository weaponRepository;
    private final WeaponIssueRepository weaponIssueRepository;
    private final UserRepository userRepository;
    private final BulletRepository bulletRepository;

    public WeaponIssueServiceImpl(WeaponRepository weaponRepository,
            WeaponIssueRepository weaponIssueRepository,
            UserRepository userRepository,
            BulletRepository bulletRepository) {
        this.weaponRepository = weaponRepository;
        this.weaponIssueRepository = weaponIssueRepository;
        this.userRepository = userRepository;
        this.bulletRepository = bulletRepository;
    }

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

        Weapon weapon = weaponRepository.findBySerialNumber(dto.getWeaponSerial())
                .orElseThrow(() -> new RuntimeException("Weapon not found with serial: " + dto.getWeaponSerial()));

        if (weapon.getStatus() != WeaponStatus.AVAILABLE) {
            throw new RuntimeException("Weapon not available. Current status: " + weapon.getStatus());
        }

        User issuedTo = userRepository.findById(dto.getIssuedToId())
                .orElseThrow(() -> new RuntimeException("Issued-to user not found: " + dto.getIssuedToId()));

        User handedOverBy = userRepository.findById(dto.getHandedOverById())
                .orElseThrow(() -> new RuntimeException("Handed-over user not found: " + dto.getHandedOverById()));

        // ===== BULLET STOCK VALIDATION + DECREMENT =====
        String bulletType = dto.getBulletType();
        Integer magsToIssue = dto.getNumberOfMagazines();

        Bullet stockBullet = null;
        if (bulletType != null && !bulletType.trim().isEmpty() && magsToIssue != null) {
            if (magsToIssue <= 0) {
                throw new RuntimeException("Magazines to issue must be greater than 0");
            }

            stockBullet = bulletRepository.findByBulletType(bulletType.trim())
                    .orElseThrow(() -> new RuntimeException("Bullet type not found: " + bulletType));

            int available = stockBullet.getNumberOfMagazines() != null ? stockBullet.getNumberOfMagazines() : 0;

            if (magsToIssue > available) {
                throw new RuntimeException(
                        "Not enough magazines. Available: " + available + ", Requested: " + magsToIssue);
            }

            stockBullet.setNumberOfMagazines(available - magsToIssue);
            bulletRepository.save(stockBullet);
        }

        WeaponIssue issue = new WeaponIssue();
        issue.setWeapon(weapon);
        issue.setIssuedTo(issuedTo);
        issue.setHandedOverBy(handedOverBy);
        issue.setIssuedAt(LocalDateTime.now());
        issue.setDueDate(dto.getDueDate());
        issue.setIssueNote(dto.getIssueNote());

        // ✅ Set status to ISSUED for new issue
        issue.setStatus(WeaponStatus.ISSUED);

        // Save bullet info into weapon_issues row
        if (stockBullet != null) {
            issue.setBulletType(stockBullet.getBulletType());
            issue.setIssuedMagazines(magsToIssue);
            issue.setBulletRemarks(dto.getBulletRemarks());
        }

        weapon.setStatus(WeaponStatus.ISSUED);

        weaponIssueRepository.save(issue);
        weaponRepository.save(weapon);
    }

    @Override
    @Transactional
    public void returnWeapon(ReturnWeaponRequestDTO dto) {

        // ===== VALIDATION =====
        if (dto.getWeaponSerial() == null || dto.getWeaponSerial().trim().isEmpty()) {
            throw new RuntimeException("Weapon serial number is required");
        }

        if (dto.getReceivedByUserId() == null) {
            throw new RuntimeException("Receiving user ID is required");
        }

        Weapon weapon = weaponRepository.findBySerialNumber(dto.getWeaponSerial())
                .orElseThrow(() -> new RuntimeException("Weapon not found with serial: " + dto.getWeaponSerial()));

        WeaponIssue issue = weaponIssueRepository.findByWeapon_SerialNumberAndReturnedAtIsNull(weapon.getSerialNumber())
                .orElseThrow(() -> new RuntimeException("No active issue found for this weapon"));

        User receivedBy = userRepository.findById(dto.getReceivedByUserId())
                .orElseThrow(() -> new RuntimeException("Receiving user not found: " + dto.getReceivedByUserId()));

        // Set return timestamp and receiving officer
        issue.setReturnedAt(LocalDateTime.now());
        issue.setReceivedBy(receivedBy);
        issue.setReturnNote(dto.getReturnNote() != null ? dto.getReturnNote() : "Returned");

        // ✅ CRITICAL FIX: Do NOT change the status field on return
        // The status should remain ISSUED (the original issue status)
        // The returnedAt timestamp indicates the weapon has been returned
        // DO NOT SET: issue.setStatus(WeaponStatus.AVAILABLE);
        // The weapon_issues table tracks the issue record, not the current weapon state
        // The weapon entity status is what gets updated to AVAILABLE

        // ===== BULLET RETURN VALIDATION + INCREMENT =====
        Integer issuedMags = issue.getIssuedMagazines();
        String issuedBulletType = issue.getBulletType();

        // Check if bullets were actually issued
        boolean bulletsWereIssued = (issuedBulletType != null && !issuedBulletType.trim().isEmpty() && 
                                     issuedMags != null && issuedMags > 0);

        // ✅ Only process bullet returns if bullets were issued
        if (bulletsWereIssued) {
            Integer returnedMags = dto.getReturnedMagazines();
            
            // If bullets were issued, we expect returnedMagazines to be provided
            if (returnedMags == null) {
                throw new RuntimeException("Returned magazines count is required when bullets were issued");
            }

            if (returnedMags < 0) {
                throw new RuntimeException("Returned magazines cannot be negative");
            }

            if (returnedMags > issuedMags) {
                throw new RuntimeException("Returned magazines (" + returnedMags +
                        ") cannot exceed issued magazines (" + issuedMags + ")");
            }

            // Normalize bullet type for matching
            String normalizedBulletType = issuedBulletType.trim();

            Bullet stockBullet = bulletRepository.findByBulletType(normalizedBulletType)
                    .orElseThrow(
                            () -> new RuntimeException("Bullet stock not found for type: " + normalizedBulletType));

            // Add returned magazines back to stock
            int currentStock = stockBullet.getNumberOfMagazines() != null ? stockBullet.getNumberOfMagazines() : 0;
            stockBullet.setNumberOfMagazines(currentStock + returnedMags);
            bulletRepository.save(stockBullet);

            // Record bullet return details
            issue.setReturnedMagazines(returnedMags);
            issue.setUsedBullets(dto.getUsedBullets() != null ? dto.getUsedBullets() : 0);
            issue.setBulletCondition(dto.getBulletCondition() != null ? dto.getBulletCondition() : "good");

            // Append bullet remarks if provided
            if (dto.getBulletRemarks() != null && !dto.getBulletRemarks().trim().isEmpty()) {
                String existing = issue.getBulletRemarks() != null ? issue.getBulletRemarks() : "";
                String separator = (existing.isEmpty()) ? "" : "\n";
                issue.setBulletRemarks(existing + separator + "[Return] " + dto.getBulletRemarks().trim());
            }
        }

        // ✅ Update the WEAPON status to AVAILABLE (not the issue status)
        weapon.setStatus(WeaponStatus.AVAILABLE);

        // Save both - but issue.status remains ISSUED
        weaponIssueRepository.save(issue);
        weaponRepository.save(weapon);
    }

    @Override
    public List<OfficerDTO> getAllOfficers() {
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