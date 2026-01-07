package com.crimeLink.analyzer.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeaponReturnDTO {

    private String weaponSerial;
    private Integer receivedBy;   // user id
    private String returnNote;
}
