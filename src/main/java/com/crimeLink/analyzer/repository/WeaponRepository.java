package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.enums.WeaponStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeaponRepository extends JpaRepository<Weapon, String> {
    boolean existsById(String serialNumber);
    List<Weapon> findByStatus(WeaponStatus status);
    Optional<Weapon> findBySerialNumber(String serialNumber);
}