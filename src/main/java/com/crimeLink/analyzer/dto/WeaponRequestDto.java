package com.crimeLink.analyzer.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WeaponRequestDto {
    private Integer requestId;
    private String weaponSerial;
    private Integer ammoCount;
    private Integer requestedById;
    private String requestNote;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
}
