package com.crimeLink.analyzer.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IssueWeaponRequestDTO {
    private String weaponSerial;
    private Integer issuedToId;
    private Integer handedOverById;
    private LocalDate dueDate;
    private String issueNote;

       // Bullet details (optional - can issue weapon without bullets)
    private String bulletType;
    private Integer numberOfMagazines;
    private String bulletRemarks;
}