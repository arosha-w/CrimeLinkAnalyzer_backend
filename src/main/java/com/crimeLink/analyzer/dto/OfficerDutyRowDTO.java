package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficerDutyRowDTO {
    private Integer officerId;
    private String name;
    private String location;
    private String datetime;
    private String status;
    private String description;
}
