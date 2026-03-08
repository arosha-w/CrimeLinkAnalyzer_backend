package com.crimeLink.analyzer.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "weapon_requests")
public class WeaponRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    /* Weapon */
    @ManyToOne
    @JoinColumn(name = "weapon_serial", referencedColumnName = "serial_number")
    private Weapon weapon;

    /* Issued to */
    @ManyToOne
    @JoinColumn(name = "requested_by", referencedColumnName = "user_id")
    private User requestedBy;

    private LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;
    private String requestNote;

    @Enumerated(EnumType.STRING)
    private WeaponRequestStatus status;
}
