package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.enums.WeaponStatus;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.service.WeaponService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WeaponServiceImpl implements WeaponService {

    private final WeaponRepository weaponRepository;

    public WeaponServiceImpl(WeaponRepository weaponRepository) {
        this.weaponRepository = weaponRepository;
    }

    @Override
    public Weapon addWeapon(WeaponAddDTO dto) {

        if (weaponRepository.existsById(dto.getSerialNumber())) {
            throw new RuntimeException("Weapon already exists");
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
                .orElseThrow(() -> new RuntimeException("Weapon not found"));

        weapon.setWeaponType(dto.getWeaponType());
        weapon.setRemarks(dto.getRemarks());
        weapon.setStatus(dto.getStatus());

        return weaponRepository.save(weapon);
    }

    @Override
    public List<Weapon> getAllWeapons() {
        return weaponRepository.findAll();
    }
}


