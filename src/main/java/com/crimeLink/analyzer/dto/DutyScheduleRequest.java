package com.crimeLink.analyzer.dto;

import com.crimeLink.analyzer.enums.DutyStatus;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DutyScheduleRequest {
    private Integer officerId;
    private LocalDate date;
    private String timeRange;
    private DutyStatus status;
    private String location;
    private String description;
}




