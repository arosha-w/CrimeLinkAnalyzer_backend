package com.crimeLink.analyzer.dto;

import com.crimeLink.analyzer.enums.WeaponStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WeaponAddDTO {

    private String serialNumber;
    private String weaponType;
    private String remarks;



}
