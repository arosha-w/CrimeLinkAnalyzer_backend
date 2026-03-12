package com.crimeLink.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bullets")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Bullet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bullet_id")
    private Integer bulletId;

    @Column(name = "bullet_type", nullable = false)
    private String bulletType;

    @Column(name = "number_of_magazines", nullable = false)
    private Integer numberOfMagazines;

    @Column(name = "register_date", updatable = false)
    private LocalDateTime registerDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Column(name = "remarks")
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