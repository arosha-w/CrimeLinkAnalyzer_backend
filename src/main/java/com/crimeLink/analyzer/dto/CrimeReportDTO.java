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
    private Long longitude;
    private Long latitude;
    private String description;
    private LocalDate dateReported;
    private LocalTime timeReported;
    private String crimeType;
}
