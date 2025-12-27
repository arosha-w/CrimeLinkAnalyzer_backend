package com.crimeLink.analyzer.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class IssueWeaponRequestDTO {

    private String weaponSerial;
    private Integer officerId;
    private LocalDate dueDate;
    private String notes;
}
