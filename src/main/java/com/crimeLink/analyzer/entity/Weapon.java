package com.crimeLink.analyzer.entity;

import com.crimeLink.analyzer.enums.WeaponStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "weapons")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Weapon {

    @Id
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Column(name = "weapon_type", nullable = false)
    private String weaponType;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WeaponStatus status;


    @Column(name = "register_date", updatable = false)
    private LocalDateTime registerDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    private String remarks;

    @PrePersist
    void onCreate() {
        registerDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        updatedDate = LocalDateTime.now();
    }
}
