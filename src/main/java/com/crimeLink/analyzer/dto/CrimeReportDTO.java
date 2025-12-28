package com.crimeLink.analyzer.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CrimeReportDTO {
    private Long reportId;
    private Double longitude;
    private Double latitude;
    private String description;
    private LocalDate dateReported;
    private LocalTime timeReported;
    private String crimeType;
}
