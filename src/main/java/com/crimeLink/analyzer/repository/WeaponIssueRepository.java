package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.WeaponIssue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeaponIssueRepository extends JpaRepository<WeaponIssue, Integer> {
    List<WeaponIssue> findByWeapon_SerialNumberAndReturnedAtIsNullOrderByIssuedAtDesc(String serialNumber);
    Optional<WeaponIssue> findByWeapon_SerialNumberAndReturnedAtIsNull(String serialNumber);
}