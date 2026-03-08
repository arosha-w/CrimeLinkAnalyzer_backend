package com.crimeLink.analyzer.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crimeLink.analyzer.dto.WeaponRequestDto;
import com.crimeLink.analyzer.service.WeaponRequestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/weapon/requests")
@RequiredArgsConstructor
public class WeaponRequestController {

    private final WeaponRequestService weaponRequestService;

    @PostMapping
    public ResponseEntity<WeaponRequestDto> createRequest(@RequestBody WeaponRequestDto dto) {
        WeaponRequestDto createRequest = weaponRequestService.createRequest(dto);
        return ResponseEntity.ok(createRequest);
    }

    @GetMapping
    public ResponseEntity<List<WeaponRequestDto>> getRequestsByUser(@PathVariable Integer userId) {
        List<WeaponRequestDto> requests = weaponRequestService.getRequestsByUser(userId);
        return ResponseEntity.ok(requests);
    }

    @GetMapping("user/{userId}")
    public ResponseEntity<List<WeaponRequestDto>> getRequestsByUser(@PathVariable Integer userId) {
        List<WeaponRequestDto> requests = weaponRequestService.getRequestsByUser(userId);
        return ResponseEntity.ok(requests);
    }

    @PutMapping("/{requestId}/approve")
    public ResponseEntity<WeaponRequestDto> approveRequest(@PathVariable Integer requestId) {
        WeaponRequestDto approvedRequest = weaponRequestService.approvedRequest(requestId);

        return ResponseEntity.ok(approvedRequest);
    }

    @PutMapping("/{requestId}/reject")
    public ResponseEntity<WeaponRequestDto> rejectRequest(@PathVariable Integer requestId) {
        WeaponRequestDto rejectedRequest = weaponRequestService.rejectedRequest(requestId);

        return ResponseEntity.ok(rejectedRequest);
    }
}
