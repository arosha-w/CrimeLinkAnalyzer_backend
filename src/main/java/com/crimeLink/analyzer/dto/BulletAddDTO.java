package com.crimeLink.analyzer.dto;

import lombok.Data;

@Data
public class BulletAddDTO {
    private String bulletType;
    private Integer numberOfMagazines;
    private String remarks;
}