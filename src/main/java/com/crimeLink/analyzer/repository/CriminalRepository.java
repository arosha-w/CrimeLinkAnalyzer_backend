package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.Criminal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CriminalRepository extends JpaRepository<Criminal, String> {
    
    Optional<Criminal> findByContactNumber(String contactNumber);
    
    Optional<Criminal> findBySecondaryContact(String secondaryContact);
    
    @Query("SELECT c FROM Criminal c WHERE c.contactNumber = :phone OR c.secondaryContact = :phone")
    Optional<Criminal> findByPhoneNumber(@Param("phone") String phone);
}
