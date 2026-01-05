package com.crimeLink.analyzer.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DutyRecommendationRequest {
    private LocalDate date;
    private String location;
    private String timeRange;
    private Integer requiredOfficers;
}
