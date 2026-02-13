package com.crimeLink.analyzer.controller;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.crimeLink.analyzer.dto.LocationPointDTO;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.service.impl.LocationServiceImpl;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LocationController {
    private final LocationServiceImpl service;

    @PostMapping("/officers/me/locations/bulk")
    public void uploadMyLocations(@AuthenticationPrincipal User user, @RequestBody List<LocationPointDTO> points) {
        System.out.println("Received locations: " + points.size()); // REMOVE: for testing
        if (user == null) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"FieldOfficer".equalsIgnoreCase(user.getRole())) {
            throw new RuntimeException("Only field officers can upload locations");
        }

        String officerBadgeNo = user.getBadgeNo();
        if (officerBadgeNo == null || officerBadgeNo.isBlank()) {
            throw new RuntimeException("Badge number missing");
        }
        service.saveBulk(officerBadgeNo, points);
    }

    @GetMapping("/admin/officers/{officerBadgeNo}/locations")
    public Object history(
            @AuthenticationPrincipal User user,
            @PathVariable String officerBadgeNo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        System.out.println("📍 LocationController.history() called");
        System.out.println("   Badge: " + officerBadgeNo);
        System.out.println("   From: " + from + ", To: " + to);
        System.out.println("   User: " + (user != null ? user.getEmail() : "NULL"));
        System.out.println("   Role: " + (user != null ? user.getRole() : "NULL"));
        System.out.println("   Authorities: " + (user != null ? user.getAuthorities() : "NULL"));
        return service.getHistory(officerBadgeNo, from, to);
    }

    @GetMapping("/debug/whoami")
    public Map<String, Object> whoAmI(@AuthenticationPrincipal User user) {
        Map<String, Object> info = new HashMap<>();
        if (user != null) {
            info.put("email", user.getEmail());
            info.put("name", user.getName());
            info.put("role", user.getRole());
            info.put("authorities", user.getAuthorities().stream()
                    .map(auth -> auth.getAuthority())
                    .toList());
            info.put("userId", user.getUserId());
            info.put("badgeNo", user.getBadgeNo());
        } else {
            info.put("error", "No authenticated user");
        }
        return info;
    }

    @GetMapping("/admin/officers/{officerBadgeNo}/locations/last")
    public Object lastLocation(@PathVariable String officerBadgeNo) {
        return service.getLastLocation(officerBadgeNo);
    }
}
