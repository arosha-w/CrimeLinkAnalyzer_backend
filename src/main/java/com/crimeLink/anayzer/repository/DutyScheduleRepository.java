package com.crimeLink.anayzer.repository;

import com.crimeLink.anayzer.entity.DutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Integer> {
    List<DutySchedule> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);

    List<DutySchedule> findByAssignedOfficerAndDatetimeBetween(
            Long officerId,
            LocalDateTime start,
            LocalDateTime end
    );
}
