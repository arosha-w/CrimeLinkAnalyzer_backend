package com.crimeLink.analyzer.service;

import java.util.List;

import com.crimeLink.analyzer.entity.OfficerAnnouncement;
import com.crimeLink.analyzer.enums.AnnouncementStatus;

public interface OfficerAnnouncementService {
    public OfficerAnnouncement createAnnouncement(OfficerAnnouncement announcement);

    public List<OfficerAnnouncement> getAllAnnouncements();

    public OfficerAnnouncement getAnnouncementById(Long id);

    public OfficerAnnouncement updateAnnouncement(Long id, OfficerAnnouncement announcement);

    public OfficerAnnouncement changeStatus(Long id, AnnouncementStatus status);

    public void deleteAnnouncement(Long id);
}
