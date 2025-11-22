package com.crimeLink.anayzer.dto;

import java.time.LocalDateTime;

public class OfficerDutyRowDTO {
    private Long officerId;
    private String officerName;

    private Long scheduleId;
    private LocalDateTime datetime;

    private Integer duration;
    private String taskType;
    private String status;
    private String location;
    private String description;
}
