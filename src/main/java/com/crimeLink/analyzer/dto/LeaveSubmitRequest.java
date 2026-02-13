package com.crimeLink.analyzer.dto;

import lombok.Data;
import jakarta.validation.constraints.*;
import org.antlr.v4.runtime.misc.NotNull;

import java.time.LocalDate;

@Data
public class LeaveSubmitRequest {
    private Long officerId;

    private LocalDate date;

    private String reason;
}
