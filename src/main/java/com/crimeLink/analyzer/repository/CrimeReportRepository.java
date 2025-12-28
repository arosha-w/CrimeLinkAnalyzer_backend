package com.crimeLink.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crimeLink.analyzer.entity.CrimeReport;

@Repository
public interface CrimeReportRepository extends JpaRepository<CrimeReport, Long> {
    // List<CrimeReport> findByReportId(Long reportId);
}
