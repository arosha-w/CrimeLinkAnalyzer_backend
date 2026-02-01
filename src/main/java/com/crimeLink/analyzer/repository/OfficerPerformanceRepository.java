package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.OfficerPerformance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OfficerPerformanceRepository extends JpaRepository<OfficerPerformance, Long> {

    // IMPORTANT: returns a LIST (no more NonUniqueResultException from single result)
    List<OfficerPerformance> findByOfficer_UserId(Integer userId);

}
