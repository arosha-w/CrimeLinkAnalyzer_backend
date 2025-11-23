package com.crimeLink.anayzer.repository;

import com.crimeLink.anayzer.entity.DutySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DutyScheduleRepository extends JpaRepository<DutySchedule, Long> {

    // for one officer all duties
    List<DutySchedule> findByAssignedOfficer_UserId(Integer officerId);

    // for one officer duties within a date-range
    List<DutySchedule> findByAssignedOfficer_UserIdAndDatetimeBetween(
            Integer officerId,
            LocalDateTime start,
            LocalDateTime end
    );

    // for calendar range loading
    List<DutySchedule> findByDatetimeBetween(LocalDateTime start, LocalDateTime end);
}
