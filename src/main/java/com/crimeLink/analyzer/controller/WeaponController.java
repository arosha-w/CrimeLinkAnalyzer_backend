package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.WeaponCreateDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.service.WeaponService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weapon")
@CrossOrigin(origins = "http://localhost:5173")
public class WeaponController {

    private final WeaponService weaponService;

    public WeaponController(WeaponService weaponService) {
        this.weaponService = weaponService;
    }

    @PostMapping("/add-weapon")
    public ResponseEntity<Weapon> addWeapon(@RequestBody WeaponCreateDTO dto) {
        return new ResponseEntity<>(weaponService.addWeapon(dto), HttpStatus.CREATED);
    }

    @PutMapping("/weapon-update/{serialNumber}")
    public ResponseEntity<Weapon> updateWeapon(
            @PathVariable String serialNumber,
            @RequestBody WeaponUpdateDTO dto
    ) {
        return ResponseEntity.ok(weaponService.updateWeapon(serialNumber, dto));
    }

    @GetMapping("/all")
    public ResponseEntity<List<Weapon>> getAllWeapons() {
        return ResponseEntity.ok(weaponService.getAllWeapons());
    }
}
