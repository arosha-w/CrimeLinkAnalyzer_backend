package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.WeaponCreateDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;

public interface WeaponService {
    Weapon addWeapon(WeaponCreateDTO dto);

    Weapon updateWeapon(String serialNumber, WeaponUpdateDTO dto);
}
