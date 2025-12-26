package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.Weapon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeaponRepository extends JpaRepository<Weapon,Integer> {
    boolean existsById(String serialNumber);
}
