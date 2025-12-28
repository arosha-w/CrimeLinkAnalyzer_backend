package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.DutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Long> {

    List<DutySchedule> findByAssignedOfficer_UserIdAndDate(
            Long userId, LocalDate date
    );

    List<DutySchedule> findByDateBetween(
            LocalDate start, LocalDate end
    );

    List<DutySchedule> findByDate(LocalDate date);
}
