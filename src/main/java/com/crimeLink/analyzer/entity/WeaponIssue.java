package com.crimeLink.analyzer.entity;

import com.crimeLink.analyzer.enums.WeaponIssueStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "weapon_issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeaponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issueId;

    /*  Weapon */
    @ManyToOne
    @JoinColumn(name = "weapon_serial", referencedColumnName = "serial_number")
    private Weapon weapon;

    /*  ISSUED TO (uses the weapon) */
    @ManyToOne
    @JoinColumn(name = "issued_to")
    private User issuedTo;

    /*  HANDED OVER BY (store / OIC) */
    @ManyToOne
    @JoinColumn(name = "handed_over_by")
    private User handedOverBy;

    /*  RECEIVED BY (return accepting officer) */
    @ManyToOne
    @JoinColumn(name = "received_by")
    private User receivedBy;

    /*  Issue info */
    private LocalDate issuedDate;
    private String issuedTime;
    private LocalDate dueDate;

    /*  Return info */
    private LocalDate returnedDate;
    private String returnedTime;

    private String issueNote;
    private String returnNote;

    @Enumerated(EnumType.STRING)
    private WeaponIssueStatus status;
}
