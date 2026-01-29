package com.crimeLink.analyzer.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IssuedWeaponResponseDTO {

        private Integer issueId;
        private String weaponSerial;

        private Integer issuedOfficerId;
        private String issuedOfficerName;

        private LocalDate dueDate;
}
