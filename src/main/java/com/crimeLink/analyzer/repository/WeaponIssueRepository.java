package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.WeaponIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeaponIssueRepository extends JpaRepository<WeaponIssue, Integer> {
    Optional<WeaponIssue> findByWeapon_SerialNumberAndReturnedAtIsNull(String serialNumber);
}