package com.crimeLink.analyzer.entity;

import com.crimeLink.analyzer.enums.WeaponStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "weapon_issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeaponIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issueId;

    /* Weapon */
    @ManyToOne
    @JoinColumn(name = "weapon_serial", referencedColumnName = "serial_number")
    private Weapon weapon;

    /* Issued to */
    @ManyToOne
    @JoinColumn(name = "issued_to", referencedColumnName = "user_id")
    private User issuedTo;

    /* Handed over by */
    @ManyToOne
    @JoinColumn(name = "handed_over_by", referencedColumnName = "user_id")
    private User handedOverBy;

    /* Received by */
    @ManyToOne
    @JoinColumn(name = "received_by", referencedColumnName = "user_id")
    private User receivedBy;

    private LocalDateTime issuedAt;
    private LocalDate dueDate;
    private LocalDateTime returnedAt;

    private String issueNote;
    private String returnNote;

    @Enumerated(EnumType.STRING)
    private WeaponStatus status;
}