package com.crimeLink.analyzer.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DutyRecommendationRequest {
    private LocalDate date;
    private String location;
    private String timeRange;      // e.g. "06:00-21:00" (or optional)
    private Integer requiredOfficers; // how many you want suggested
}
