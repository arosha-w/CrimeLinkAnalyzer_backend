package com.crimeLink.analyzer.dto;


import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class WeaponIssuedResponseDTO {


        // Weapon
        private String weaponSerial;
        private String weaponType;

        // Issued officer
        private Integer issuedToId;
        private String issuedToName;
        private String issuedToBadge;
        private String issuedToRole;

        // Handed over officer
        private Integer handedOverById;
        private String handedOverByName;
        private String handedOverByBadge;

        // Received officer
        private Integer receivedById;
        private String receivedByName;
        private String receivedByBadge;

        // Dates
        private LocalDate issuedDate;
        private LocalTime issuedTime;
        private LocalDate dueDate;
        private LocalDate returnedDate;
        private LocalTime returnedTime;

        private String issueNote;
        private String returnNote;
        private String status;


}
