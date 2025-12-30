package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponIssueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class WeaponIssueServiceImpl implements WeaponIssueService {

    @Autowired
    private WeaponRepository weaponRepository;

    @Override
    public List<WeaponAddDTO> getAllActiveWeapons() {

        List<Weapon> weapons =
                weaponRepository.findByStatus(WeaponStatus.ACTIVE);

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

}
