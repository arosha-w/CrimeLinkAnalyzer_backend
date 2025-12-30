package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.service.WeaponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weapon")
@CrossOrigin(origins = "*")
public class WeaponController {

    private final WeaponService weaponService;

    public WeaponController(WeaponService weaponService) {
        this.weaponService = weaponService;
    }

    @PostMapping("/add-weapon")
    public ResponseEntity<?> addWeapon(@RequestBody WeaponAddDTO dto) {
        try {
            Weapon weapon = weaponService.addWeapon(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(weapon);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @PutMapping("/weapon-update/{serialNumber}")
    public ResponseEntity<?> updateWeapon(
            @PathVariable String serialNumber,
            @RequestBody WeaponUpdateDTO dto
    ) {
        try {
            Weapon weapon = weaponService.updateWeapon(serialNumber, dto);
            return ResponseEntity.ok(weapon);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<List<Weapon>> getAllWeapons() {
        return ResponseEntity.ok(weaponService.getAllWeapons());
    }
}
