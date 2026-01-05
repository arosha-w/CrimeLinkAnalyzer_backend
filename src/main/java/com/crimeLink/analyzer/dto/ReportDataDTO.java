package com.crimeLink.analyzer.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class ReportDataDTO {
    private String reportType;
    private LocalDateTime generatedAt;
    private LocalDate dateFrom;
    private LocalDate dateTo;
    private List<Map<String, Object>> data;
    private Map<String, Object> summary;
}
