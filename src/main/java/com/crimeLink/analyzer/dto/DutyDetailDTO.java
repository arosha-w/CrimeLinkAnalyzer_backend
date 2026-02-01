package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DutyDetailDTO {
    private Long id;
    private String taskType;
    private String location;
    private String timeRange;
    private String status;
    private String teamName;
    private Integer teamSize;
    private String description;
}