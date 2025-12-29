package com.crimeLink.analyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.crimeLink.analyzer.dto.CrimeLocationDTO;
import com.crimeLink.analyzer.entity.CrimeReport;

@Repository
public interface CrimeReportRepository extends JpaRepository<CrimeReport, Long> {
    // List<CrimeReport> findByReportId(Long reportId);

    @Query("""
                SELECT new com.crimeLink.analyzer.dto.CrimeLocationDTO(
                c.latitude,
                c.longitude,
                c.crimeType
            )
            FROM CrimeReport c
                """)
    List<CrimeLocationDTO> findCrimeLocations();
}
