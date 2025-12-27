package com.crimeLink.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Weapon_Issue")
public class WeaponIssue {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer issueId;

    @ManyToOne
    @JoinColumn(name = "weapon_serial", referencedColumnName = "serial_number")
    private Weapon weapon;

    @ManyToOne
    @JoinColumn(name = "officer_id", referencedColumnName = "user_id")
    private User officer;

    /* Issue info */
    private LocalDate issueDate;
    private String issueTime;

    private LocalDate dueDate;

    /* Return info */
    private LocalDate returnDate;
    private String returnTime;

    private String issueNote;
    private String returnRemark;

    @Enumerated(EnumType.STRING)
    private WeaponIssue status;

}
