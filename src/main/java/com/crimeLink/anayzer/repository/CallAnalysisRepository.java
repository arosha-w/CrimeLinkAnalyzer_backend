package com.crimeLink.anayzer.repository;

import com.crimeLink.anayzer.entity.CallAnalysisRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CallAnalysisRepository extends JpaRepository<CallAnalysisRecord, String> {
    
    List<CallAnalysisRecord> findByUploadedByOrderByCreatedAtDesc(String uploadedBy);
    
    List<CallAnalysisRecord> findByStatusOrderByCreatedAtDesc(String status);
    
    List<CallAnalysisRecord> findTop10ByOrderByCreatedAtDesc();
}
