package com.crimeLink.analyzer.dto;

import com.crimeLink.analyzer.enums.WeaponStatus;
import lombok.Data;

@Data
public class WeaponCreateDTO {

    private String serialNumber;
    private String weaponType;
    private String remarks;
}
