package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.dto.WeaponResponseDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;

import java.util.List;

public interface WeaponService {
    Weapon addWeapon(WeaponAddDTO dto);
    Weapon updateWeapon(String serialNumber, WeaponUpdateDTO dto);
    List<Weapon> getAllWeapons();
    Weapon getWeaponBySerial(String serialNumber);
    List<WeaponResponseDTO> getAllWeaponsWithDetails();
}