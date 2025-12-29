package com.crimeLink.analyzer.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class WeaponRequestOfficerDTO {
    private Integer userId;
    private String name;
    private String role;
    private String badgeNo;
    private String status;
}
