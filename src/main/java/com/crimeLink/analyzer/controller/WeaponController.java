package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.WeaponAddDTO;
import com.crimeLink.analyzer.dto.WeaponResponseDTO;
import com.crimeLink.analyzer.dto.WeaponUpdateDTO;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.service.WeaponService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/weapon")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WeaponController {

    private final WeaponService weaponService;

    @PostMapping("/add-weapon")
    public ResponseEntity<?> addWeapon(@RequestBody WeaponAddDTO dto) {
        try {
            Weapon weapon = weaponService.addWeapon(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(weapon);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    @PutMapping("/weapon-update/{serialNumber}")
    public ResponseEntity<?> updateWeapon(
            @PathVariable String serialNumber,
            @RequestBody WeaponUpdateDTO dto) {
        try {
            Weapon weapon = weaponService.updateWeapon(serialNumber, dto);
            return ResponseEntity.ok(weapon);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllWeapons() {
        try {
            List<Weapon> weapons = weaponService.getAllWeapons();
            return ResponseEntity.ok(weapons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch weapons"));
        }
    }

    @GetMapping("/all-with-details")
    public ResponseEntity<?> getAllWeaponsWithDetails() {
        try {
            List<WeaponResponseDTO> weapons = weaponService.getAllWeaponsWithDetails();
            return ResponseEntity.ok(weapons);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch weapons with details"));
        }
    }

    @GetMapping("/{serialNumber}")
    public ResponseEntity<?> getWeaponBySerial(@PathVariable String serialNumber) {
        try {
            Weapon weapon = weaponService.getWeaponBySerial(serialNumber);
            return ResponseEntity.ok(weapon);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch weapon"));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}