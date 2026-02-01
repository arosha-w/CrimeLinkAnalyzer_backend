package com.crimeLink.analyzer.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class LeaveUpdateRequest {
    @NotBlank
    private String status; // APPROVED or DENIED

    private String responseReason;
}
