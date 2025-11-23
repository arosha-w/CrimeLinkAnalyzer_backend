package com.crimeLink.anayzer.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DutyScheduleRequest {
    private Integer officerId;
    private LocalDateTime datetime;
    private Integer duration;
    private String taskType;
    private String status;
    private String location;
    private String description;
}




