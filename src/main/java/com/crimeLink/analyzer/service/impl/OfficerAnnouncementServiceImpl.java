package com.crimeLink.analyzer.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.crimeLink.analyzer.entity.OfficerAnnouncement;
import com.crimeLink.analyzer.enums.AnnouncementStatus;
import com.crimeLink.analyzer.repository.OfficerAnnouncementRepository;
import com.crimeLink.analyzer.service.OfficerAnnouncementService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OfficerAnnouncementServiceImpl implements OfficerAnnouncementService {

    private final OfficerAnnouncementRepository announcementRepository;

    public OfficerAnnouncement createAnnouncement(OfficerAnnouncement announcement) {
        announcement.setId(null);
        announcement.setDate(new Date());
        if (announcement.getStatus() == null) {
            announcement.setStatus(AnnouncementStatus.ACTIVE);
        }
        return announcementRepository.save(announcement);
    }

    public List<OfficerAnnouncement> getAllAnnouncements() {
        return announcementRepository.findAll();
    }

    public OfficerAnnouncement getAnnouncementById(Long id) {
        return announcementRepository.findById(id).orElseThrow(() -> new RuntimeException("Announcement not found"));
    }

    public OfficerAnnouncement updateAnnouncement(Long id, OfficerAnnouncement announcement) {
        OfficerAnnouncement existing = getAnnouncementById(id);
        existing.setTitle(announcement.getTitle());
        existing.setMessage(announcement.getMessage());
        existing.setTag(announcement.getTag());
        existing.setStatus(announcement.getStatus());
        return announcementRepository.save(existing);
    }

    public OfficerAnnouncement changeStatus(Long id, AnnouncementStatus status) {
        OfficerAnnouncement existing = getAnnouncementById(id);
        existing.setStatus(status);
        return announcementRepository.save(existing);
    }

    public void deleteAnnouncement(Long id) {
        if(!announcementRepository.existsById(id)) {
            throw new RuntimeException("Announcement not found");
        }
        announcementRepository.deleteById(id);
    }
}
