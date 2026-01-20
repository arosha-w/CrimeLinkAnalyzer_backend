package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.DutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Long> {

    Optional<DutySchedule> findByDateAndAssignedOfficer_UserIdAndTimeRange(
            LocalDate date,
            Integer userId,
            String timeRange
    );
    List<DutySchedule> findByDateBetween(
            LocalDate start, LocalDate end
    );

    List<DutySchedule> findByAssignedOfficer_UserId(Long officerId);

    List<DutySchedule> findByDateAndAssignedOfficer_UserId(LocalDate date, Long officerId);

    List<DutySchedule> findByDate(LocalDate date);
    Optional<DutySchedule> findByDateAndAssignedOfficer_UserId(
            LocalDate date,
            Integer userId
    );
    // total duties ever
    long countByAssignedOfficer_UserId(Integer userId);

    // duties in recent period (e.g. last 7 days)
    long countByAssignedOfficer_UserIdAndDateBetween(
            Integer userId,
            LocalDate start,
            LocalDate end
    );
}
