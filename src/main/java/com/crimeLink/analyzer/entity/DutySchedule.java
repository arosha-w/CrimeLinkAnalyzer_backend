package com.crimeLink.analyzer.entity;

import com.crimeLink.analyzer.enums.DutyStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "duty_schedule")
@Getter
@Setter
public class DutySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long id; // ✅ single primary key

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_officer", nullable = false)
    private User assignedOfficer;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "time_range")
    private String timeRange;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DutyStatus status;

    @Column(name = "location")
    private String location;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
