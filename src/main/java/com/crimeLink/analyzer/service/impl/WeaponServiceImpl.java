package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.OfficerDTO;
import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.dto.WeaponResponseDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.entity.WeaponIssue;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.WeaponIssueRepository;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeaponServiceImpl implements WeaponService {

    private final WeaponRepository weaponRepository;
    private final WeaponIssueRepository weaponIssueRepository;

    @Override
    public Weapon addWeapon(WeaponAddDTO dto) {
        if (weaponRepository.existsById(dto.getSerialNumber())) {
            throw new RuntimeException("Weapon already exists with serial number: " + dto.getSerialNumber());
        }

        Weapon weapon = new Weapon();
        weapon.setSerialNumber(dto.getSerialNumber());
        weapon.setWeaponType(dto.getWeaponType());
        weapon.setRemarks(dto.getRemarks());
        weapon.setStatus(WeaponStatus.AVAILABLE);

        return weaponRepository.save(weapon);
    }

    @Override
    public Weapon updateWeapon(String serialNumber, WeaponUpdateDTO dto) {
        Weapon weapon = weaponRepository.findById(serialNumber)
                .orElseThrow(() -> new RuntimeException("Weapon not found with serial number: " + serialNumber));

        weapon.setWeaponType(dto.getWeaponType());
        weapon.setRemarks(dto.getRemarks());
        weapon.setStatus(dto.getStatus());

        return weaponRepository.save(weapon);
    }

    @Override
    public List<Weapon> getAllWeapons() {
        return weaponRepository.findAll();
    }

    @Override
    public Weapon getWeaponBySerial(String serialNumber) {
        return weaponRepository.findById(serialNumber)
                .orElseThrow(() -> new RuntimeException("Weapon not found with serial number: " + serialNumber));
    }

    @Override
    public List<WeaponResponseDTO> getAllWeaponsWithDetails() {
        List<Weapon> weapons = weaponRepository.findAll();

        return weapons.stream()
                .map(weapon -> {
                    WeaponResponseDTO dto = new WeaponResponseDTO();
                    dto.setSerialNumber(weapon.getSerialNumber());
                    dto.setWeaponType(weapon.getWeaponType());
                    dto.setStatus(weapon.getStatus().toString());
                    dto.setRemarks(weapon.getRemarks());

                    // If weapon is issued, get issue details
                    if (weapon.getStatus() == WeaponStatus.ISSUED) {
                        Optional<WeaponIssue> issueOpt = weaponIssueRepository
                                .findByWeapon_SerialNumberAndReturnedAtIsNull(weapon.getSerialNumber());

                        if (issueOpt.isPresent()) {
                            WeaponIssue issue = issueOpt.get();

                            // Issued To Officer
                            if (issue.getIssuedTo() != null) {
                                OfficerDTO issuedToDTO = new OfficerDTO();
                                issuedToDTO.setId(issue.getIssuedTo().getUserId());
                                issuedToDTO.setName(issue.getIssuedTo().getName());
                                issuedToDTO.setServiceId(issue.getIssuedTo().getBadgeNo());
                                issuedToDTO.setBadge(issue.getIssuedTo().getBadgeNo());
                                issuedToDTO.setRole(issue.getIssuedTo().getRole());
                                issuedToDTO.setRank(issue.getIssuedTo().getRole());
                                issuedToDTO.setStatus(issue.getIssuedTo().getStatus());
                                dto.setIssuedTo(issuedToDTO);
                            }

                            // Handed Over By Officer
                            if (issue.getHandedOverBy() != null) {
                                OfficerDTO handedOverByDTO = new OfficerDTO();
                                handedOverByDTO.setId(issue.getHandedOverBy().getUserId());
                                handedOverByDTO.setName(issue.getHandedOverBy().getName());
                                handedOverByDTO.setServiceId(issue.getHandedOverBy().getBadgeNo());
                                handedOverByDTO.setBadge(issue.getHandedOverBy().getBadgeNo());
                                handedOverByDTO.setRole(issue.getHandedOverBy().getRole());
                                handedOverByDTO.setRank(issue.getHandedOverBy().getRole());
                                handedOverByDTO.setStatus(issue.getHandedOverBy().getStatus());
                                dto.setHandedOverBy(handedOverByDTO);
                            }

                            // Dates
                            if (issue.getIssuedAt() != null) {
                                dto.setIssuedDate(issue.getIssuedAt().toLocalDate().toString());
                            }
                            if (issue.getDueDate() != null) {
                                dto.setDueDate(issue.getDueDate().toString());
                            }
                            dto.setIssueNote(issue.getIssueNote());
                        }
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}