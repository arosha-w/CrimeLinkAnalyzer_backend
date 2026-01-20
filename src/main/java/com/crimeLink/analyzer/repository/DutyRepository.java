package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.Duty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Duty Repository - For Mobile App Queries Only
 */
@Repository
public interface DutyRepository extends JpaRepository<Duty, Long> {

    /**
     * Find all duties for a specific officer
     * Used for: Calendar view - showing all duty dates
     */
    List<Duty> findByOfficerId(Long officerId);

    /**
     * Find duties for a specific officer on a specific date
     * Used for: When officer selects a date to view details
     */
    @Query("SELECT d FROM Duty d WHERE d.officerId = :officerId " +
            "AND DATE(d.date) = DATE(:date) " +
            "ORDER BY d.date ASC")
    List<Duty> findByOfficerIdAndDate(
            @Param("officerId") Long officerId,
            @Param("date") LocalDateTime date
    );
}