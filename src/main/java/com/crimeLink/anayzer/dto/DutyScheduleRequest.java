package com.crimeLink.anayzer.dto;

import lombok.Data;

@Data
public class DutyScheduleRequest {

    private String datetime;
    private String duration;
    private String taskType;
    private String status;

    private Long assignedOfficer;

    private String location;
    private String description;
}


