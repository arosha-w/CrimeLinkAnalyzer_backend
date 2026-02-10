package com.crimeLink.analyzer.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "location_points", indexes = {
        @Index(name = "idx_location_points_officer_ts", columnList = "officer_badge_no, ts") })
public class LocationPoint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "officer_badge_no", nullable = false, length = 20)
    private String officerBadgeNo;
    private Instant ts;
    private double latitude;
    private double longitude;

    private Float accuracyM;
    private Float speedMps;
    private Float headingDeg;

    private String provider;

    @Column(columnDefinition = "jsonb")
    private String meta;
}
