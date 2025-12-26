package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.WeaponCreateDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;

public class WeaponServiceImpl implements WeaponService {
    @Autowired
    WeaponRepository weaponRepository;

    @Override
    public Weapon addWeapon(WeaponCreateDTO dto) {

        if (weaponRepository.existsById(dto.getSerialNumber())) {
            throw new IllegalArgumentException("Weapon already exists with serial: " + dto.getSerialNumber());
        }

        Weapon weapon = new Weapon();
        weapon.setSerialNumber(dto.getSerialNumber());
        weapon.setWeaponType(dto.getWeaponType());
        weapon.setStatus(dto.getStatus());
        weapon.setRemarks(dto.getRemarks());

        return weaponRepository.save(weapon);
    }


    // ================= UPDATE WEAPON =================
    @Override
    public Weapon updateWeapon(String serialNumber, WeaponUpdateDTO dto) {

        Weapon weapon = weaponRepository.findById(serialNumber)
                .orElseThrow(() ->
                        new ChangeSetPersister.NotFoundException("Weapon not found with serial: " + serialNumber)
                );

        weapon.setWeaponType(dto.getWeaponType());
        weapon.setStatus(dto.getStatus());
        weapon.setRemarks(dto.getRemarks());

        return weaponRepository.save(weapon);
    }
}
