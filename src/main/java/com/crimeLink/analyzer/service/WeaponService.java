package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.dto.WeaponResponseDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WeaponService {
    Weapon addWeapon(WeaponAddDTO dto);
    Weapon updateWeapon(String serialNumber, WeaponUpdateDTO dto);
    List<Weapon> getAllWeapons();
    Weapon getWeaponBySerial(String serialNumber);
    List<WeaponResponseDTO> getAllWeaponsWithDetails();
    List<Weapon> getWeaponsIssuedToOfficer(Integer officerId);
    String uploadWeaponPhoto(String serialNumber, MultipartFile photo);
}