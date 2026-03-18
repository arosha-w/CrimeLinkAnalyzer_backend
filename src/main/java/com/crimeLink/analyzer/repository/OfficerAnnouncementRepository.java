package com.crimeLink.analyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crimeLink.analyzer.entity.OfficerAnnouncement;
import com.crimeLink.analyzer.enums.AnnouncementStatus;
import com.crimeLink.analyzer.enums.AnnouncementTags;

@Repository
public interface OfficerAnnouncementRepository extends JpaRepository<OfficerAnnouncement, Long> {
    List<OfficerAnnouncement> findByStatus(AnnouncementStatus status);

    List<OfficerAnnouncement> findByTag(AnnouncementTags tag);
}
