package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulletResponseDTO {
    private Integer bulletId;
    private String bulletType;
    private Integer numberOfMagazines;
    private String remarks;
    private String registerDate;
}