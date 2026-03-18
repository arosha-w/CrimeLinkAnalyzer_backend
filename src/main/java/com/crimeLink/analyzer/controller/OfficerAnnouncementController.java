package com.crimeLink.analyzer.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.crimeLink.analyzer.entity.OfficerAnnouncement;
import com.crimeLink.analyzer.enums.AnnouncementStatus;
import com.crimeLink.analyzer.service.OfficerAnnouncementService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/announcements")
@RequiredArgsConstructor
public class OfficerAnnouncementController {
    private final OfficerAnnouncementService announcementService;

    @PostMapping
    public ResponseEntity<OfficerAnnouncement> createAnnouncement(
            @RequestBody OfficerAnnouncement announcement) {
        OfficerAnnouncement created = announcementService.createAnnouncement(announcement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<OfficerAnnouncement>> getAllAnnouncements() {
        return ResponseEntity.ok(announcementService.getAllAnnouncements());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfficerAnnouncement> getAnnouncementById(@PathVariable Long id) {
        return ResponseEntity.ok(announcementService.getAnnouncementById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OfficerAnnouncement> updateAnnouncement(
            @PathVariable Long id,
            @RequestBody OfficerAnnouncement updatedData) {
        return ResponseEntity.ok(announcementService.updateAnnouncement(id, updatedData));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OfficerAnnouncement> changeStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String statusValue = body.get("status");
        if (statusValue == null || statusValue.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            AnnouncementStatus newStatus = AnnouncementStatus.valueOf(statusValue.toUpperCase());
            return ResponseEntity.ok(announcementService.changeStatus(id, newStatus));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return ResponseEntity.noContent().build();
    }
}
