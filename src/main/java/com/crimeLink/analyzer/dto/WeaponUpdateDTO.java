package com.crimeLink.analyzer.dto;

import com.crimeLink.analyzer.enums.WeaponStatus;
import lombok.Data;

@Data
public class WeaponUpdateDTO {
    private String weaponType;
    private WeaponStatus status;
    private String remarks;

}
