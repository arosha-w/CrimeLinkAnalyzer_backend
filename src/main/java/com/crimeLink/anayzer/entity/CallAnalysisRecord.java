package com.crimeLink.anayzer.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "call_analysis_records")
public class CallAnalysisRecord {

    @Id
    @Column(name = "analysis_id", length = 255)
    private String analysisId;

    @Column(name = "file_name", length = 500)
    private String fileName;

    @Column(name = "uploaded_by", length = 100)
    private String uploadedBy;

    @Column(name = "total_calls")
    private Integer totalCalls;

    @Column(name = "unique_numbers_count")
    private Integer uniqueNumbersCount;

    @Column(name = "criminal_matches_count")
    private Integer criminalMatchesCount;

    @Column(name = "risk_score")
    private Integer riskScore;

    @Column(name = "status", length = 50)
    private String status; // processing, completed, failed

    @Column(name = "analysis_data", columnDefinition = "TEXT")
    private String analysisData; // JSON string of full analysis

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "processing";
        }
    }

    // Constructors
    public CallAnalysisRecord() {}

    public CallAnalysisRecord(String analysisId, String fileName, String uploadedBy) {
        this.analysisId = analysisId;
        this.fileName = fileName;
        this.uploadedBy = uploadedBy;
    }

    // Getters and Setters
    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Integer getTotalCalls() {
        return totalCalls;
    }

    public void setTotalCalls(Integer totalCalls) {
        this.totalCalls = totalCalls;
    }

    public Integer getUniqueNumbersCount() {
        return uniqueNumbersCount;
    }

    public void setUniqueNumbersCount(Integer uniqueNumbersCount) {
        this.uniqueNumbersCount = uniqueNumbersCount;
    }

    public Integer getCriminalMatchesCount() {
        return criminalMatchesCount;
    }

    public void setCriminalMatchesCount(Integer criminalMatchesCount) {
        this.criminalMatchesCount = criminalMatchesCount;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(Integer riskScore) {
        this.riskScore = riskScore;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAnalysisData() {
        return analysisData;
    }

    public void setAnalysisData(String analysisData) {
        this.analysisData = analysisData;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
