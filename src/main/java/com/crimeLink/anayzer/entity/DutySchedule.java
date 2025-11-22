package com.crimeLink.anayzer.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "duty_schedule")
public class DutySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int scheduleId;

    private String datetime;
    private String duration;
    private String taskType;
    private String status;

    private Long assignedOfficer;   // FK → users(id)

    private String location;
    private String description;
}
