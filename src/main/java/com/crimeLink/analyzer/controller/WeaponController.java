package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.WeaponCreateDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.service.WeaponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weapon")
public class WeaponController {
    private WeaponService weaponService;

    // ================= ADD WEAPON =================
    @PostMapping("/add-weapon")
    public ResponseEntity<Weapon> addWeapon(@RequestBody WeaponCreateDTO dto) {
        Weapon savedWeapon = weaponService.addWeapon(dto);
        return new ResponseEntity<>(savedWeapon, HttpStatus.CREATED);
    }

    // update weapon
    @PutMapping("/{serialNumber}")
    public ResponseEntity<Weapon> updateWeapon(
            @PathVariable String serialNumber,
            @RequestBody WeaponUpdateDTO dto
    ) {
        Weapon updatedWeapon = weaponService.updateWeapon(serialNumber, dto);
        return ResponseEntity.ok(updatedWeapon);
    }

}
