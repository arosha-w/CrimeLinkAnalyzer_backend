package com.crimeLink.analyzer.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class WeaponReturnResponseDTO {

    // Weapon
    private String weaponSerial;
    private String weaponType;

    // Issued officer
    private Integer issuedToId;
    private String issuedToName;
    private String issuedToBadge;
    private String issuedToRole;

    // Received officer
    private Integer receivedById;
    private String receivedByName;
    private String receivedByBadge;

    private LocalDate dueDate;
    private LocalDate returnedDate;
    private LocalTime returnedTime;

    private String returnNote;
    private String status;
}
