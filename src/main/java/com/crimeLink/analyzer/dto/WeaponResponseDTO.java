package com.crimeLink.analyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeaponResponseDTO {
    private String serialNumber;
    private String weaponType;
    private String status;
    private String remarks;

    // Issue details (if issued)
    private OfficerDTO issuedTo;
    private OfficerDTO handedOverBy;
    private String issuedDate;
    private String dueDate;
    private String issueNote;
}