package com.crimeLink.analyzer.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.crimeLink.analyzer.entity.LocationPoint;

public interface LocationPointRepository extends JpaRepository<LocationPoint, Long> {
    List<LocationPoint> findByOfficerBadgeNoAndTsBetweenOrderByTsAsc(String officerBadgeNo, Instant from, Instant to);

    List<LocationPoint> findByOfficerBadgeNoOrderByTsDesc(String officerBadgeNo, Pageable pageable);
}
