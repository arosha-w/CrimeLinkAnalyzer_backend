package com.crimeLink.analyzer.dto;

import lombok.Data;

@Data
public class BulletUpdateDTO {
    private String bulletType;
    private Integer numberOfMagazines;
    private String remarks;
}