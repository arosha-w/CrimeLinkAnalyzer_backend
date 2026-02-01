package com.crimeLink.analyzer.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveRequestDTO {
    private Long id;
    private Long officerId;
    private String officerName;
    private LocalDate date;
    private String reason;
    private String status;
    private LocalDate requestedDate;
    private String responseReason;
    private Long respondedBy;
    private LocalDateTime respondedDate;
}