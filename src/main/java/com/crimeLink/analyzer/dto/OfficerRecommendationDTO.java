package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class OfficerRecommendationDTO {
    private Integer officerId;
    private String name;
    private String badgeNo;             // if you have it in User
    private Double recommendationScore; // 0–100
    private String availabilityStatus;
    private LocalDate lastDutyDate;
    private Integer totalDuties;
    private Boolean locationMatch;
    private String reason;              // text explanation
}

