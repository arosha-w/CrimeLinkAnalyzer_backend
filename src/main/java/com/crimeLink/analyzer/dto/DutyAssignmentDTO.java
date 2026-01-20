package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTOs for Mobile App API Responses
 */

/**
 * DTO for calendar view - shows all duties for marking calendar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class DutyAssignmentDTO {
    private Long id;
    private String officerId;
    private String officerName;
    private String date; // ISO format: "2023-10-24T08:00:00"
    private String location;
    private String timeRange;
    private String status;
    private String taskType;
    private String description;
    private Integer duration;
}

