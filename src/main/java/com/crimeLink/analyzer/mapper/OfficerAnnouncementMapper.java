package com.crimeLink.analyzer.mapper;

import com.crimeLink.analyzer.dto.OfficerAnnouncementDTO;
import com.crimeLink.analyzer.entity.OfficerAnnouncement;

public class OfficerAnnouncementMapper {
    public static OfficerAnnouncementDTO mapToDTO(OfficerAnnouncement entity) {
        if (entity == null) {
            return null;
        }
        OfficerAnnouncementDTO dto = new OfficerAnnouncementDTO();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setMessage(entity.getMessage());
        dto.setDate(entity.getDate());
        dto.setTag(entity.getTag());
        dto.setStatus(entity.getStatus());
        return dto;
    }

    public static OfficerAnnouncement mapToEntity(OfficerAnnouncementDTO dto) {
        if (dto == null) {
            return null;
        }
        OfficerAnnouncement entity = new OfficerAnnouncement();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setMessage(dto.getMessage());
        entity.setDate(dto.getDate());
        entity.setTag(dto.getTag());
        entity.setStatus(dto.getStatus());
        return entity;
    }
}
