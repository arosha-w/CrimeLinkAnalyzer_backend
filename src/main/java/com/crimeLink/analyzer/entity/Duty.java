package com.crimeLink.analyzer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Duty Entity - For Mobile App (Field Officers)
 */
@Entity
@Table(name = "duties")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Duty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "officer_id", nullable = false)
    private Long officerId;

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "location")
    private String location;

    @Column(name = "time_range")
    private String timeRange;

    @Column(name = "status")
    private String status; // Active, Completed, Pending

    @Column(name = "task_type")
    private String taskType;

    @Column(name = "description")
    private String description;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "team_size")
    private Integer teamSize;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}