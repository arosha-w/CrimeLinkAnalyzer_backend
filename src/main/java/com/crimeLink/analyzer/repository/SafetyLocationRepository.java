package com.crimeLink.analyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crimeLink.analyzer.entity.SafetyLocation;
import com.crimeLink.analyzer.entity.SafetyType;

@Repository
public interface SafetyLocationRepository extends JpaRepository<SafetyLocation, Long> {
    List<SafetyLocation> findByType(SafetyType type);
}
