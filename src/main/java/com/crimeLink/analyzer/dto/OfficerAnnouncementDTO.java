package com.crimeLink.analyzer.dto;

import java.util.Date;

import com.crimeLink.analyzer.enums.AnnouncementStatus;
import com.crimeLink.analyzer.enums.AnnouncementTags;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OfficerAnnouncementDTO {
    private Long id;
    private String title;
    private String message;
    private Date date;
    private AnnouncementTags tag;
    private AnnouncementStatus status;
}
