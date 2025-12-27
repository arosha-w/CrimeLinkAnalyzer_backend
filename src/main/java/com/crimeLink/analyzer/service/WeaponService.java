package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.WeaponCreateDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface WeaponService {
    Weapon addWeapon(WeaponCreateDTO dto);

    Weapon updateWeapon(String serialNumber, WeaponUpdateDTO dto);

    List<Weapon> getAllWeapons();


}
