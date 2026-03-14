package com.crimeLink.analyzer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.crimeLink.analyzer.entity.WeaponRequest;

@Repository
public interface WeaponRequestRepository extends JpaRepository<WeaponRequest, Integer> {

    List<WeaponRequest> findByRequestedBy_UserId(Integer userId);

    List<WeaponRequest> findByStatus(String status);
}
