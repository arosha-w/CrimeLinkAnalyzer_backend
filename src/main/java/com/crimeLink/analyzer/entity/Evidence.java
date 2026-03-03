package com.crimeLink.analyzer.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "evidence")
public class Evidence {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String bucket;
    private String filePath;
    private String fileName;
    private String fileType;
    private Long fileSize;

    private LocalDateTime uploadTime;

    @ManyToOne
    @JoinColumn(name = "report_id", nullable = false)
    private CrimeReport crimeReport;
}
