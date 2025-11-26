package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
    List<LoginAudit> findByEmailOrderByLoginTimeDesc(String email);
    List<LoginAudit> findBySuccessFalseAndEmailAndLoginTimeAfter(String email, LocalDateTime after);
    long countBySuccessFalseAndEmailAndLoginTimeAfter(String email, LocalDateTime after);
}
