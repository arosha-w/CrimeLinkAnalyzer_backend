package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerDTO {
    private Integer id;
    private String serviceId;
    private String name;
    private String badge;
    private String role;
    private String rank;
    private String status;
}