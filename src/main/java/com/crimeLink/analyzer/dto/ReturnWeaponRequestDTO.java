package com.crimeLink.analyzer.dto;

import lombok.Data;

@Data
public class ReturnWeaponRequestDTO {
    private String weaponSerial;
    private Integer receivedByUserId;
    private String returnNote;
}