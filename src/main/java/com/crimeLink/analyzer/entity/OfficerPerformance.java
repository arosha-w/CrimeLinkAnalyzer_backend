package com.crimeLink.analyzer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "officer_performance")

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfficerPerformance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "performance_id")
    private Long performanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id", nullable = false)
    private User officer;

    @Column(name = "total_duties")
    private Integer totalDuties;

    @Column(name = "last_duty_date")
    private LocalDate lastDutyDate;


    @Column(name = "reliability_score")
    private Integer reliabilityScore; // 0–100

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

}
