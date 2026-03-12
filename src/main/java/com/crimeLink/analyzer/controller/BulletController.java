package com.crimeLink.analyzer.controller;

import com.crimeLink.analyzer.dto.BulletAddDTO;
import com.crimeLink.analyzer.dto.BulletResponseDTO;
import com.crimeLink.analyzer.dto.BulletUpdateDTO;
import com.crimeLink.analyzer.entity.Bullet;
import com.crimeLink.analyzer.service.BulletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bullet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BulletController {

    private final BulletService bulletService;

    @PostMapping("/add-bullet")
    public ResponseEntity<?> addBullet(@RequestBody BulletAddDTO dto) {
        try {
            Bullet bullet = bulletService.addBullet(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(bullet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    @PutMapping("/bullet-update/{bulletId}")
    public ResponseEntity<?> updateBullet(
            @PathVariable Integer bulletId,
            @RequestBody BulletUpdateDTO dto) {
        try {
            Bullet bullet = bulletService.updateBullet(bulletId, dto);
            return ResponseEntity.ok(bullet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("An unexpected error occurred"));
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllBullets() {
        try {
            List<Bullet> bullets = bulletService.getAllBullets();
            return ResponseEntity.ok(bullets);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch bullets"));
        }
    }

    @GetMapping("/all-with-details")
    public ResponseEntity<?> getAllBulletsWithDetails() {
        try {
            List<BulletResponseDTO> bullets = bulletService.getAllBulletsWithDetails();
            return ResponseEntity.ok(bullets);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch bullets with details: " + e.getMessage()));
        }
    }

    @GetMapping("/{bulletId}")
    public ResponseEntity<?> getBulletById(@PathVariable Integer bulletId) {
        try {
            Bullet bullet = bulletService.getBulletById(bulletId);
            return ResponseEntity.ok(bullet);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Failed to fetch bullet"));
        }
    }

    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", message);
        return response;
    }
}