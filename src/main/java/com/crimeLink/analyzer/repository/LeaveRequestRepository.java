package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.LeaveRequest;
import com.crimeLink.analyzer.enums.LeaveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByUser_UserId(Integer userId);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.date >= :startDate AND lr.date <= :endDate")
    List<LeaveRequest> findByDateBetween(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT COUNT(lr) FROM LeaveRequest lr
        WHERE lr.user.userId = :userId
          AND YEAR(lr.date) = :year
          AND MONTH(lr.date) = :month
          AND lr.status IN ('PENDING', 'APPROVED')
    """)
    int countMonthlyLeaves(@Param("userId") Integer userId,
                           @Param("year") int year,
                           @Param("month") int month);

    boolean existsByUser_UserIdAndDateAndStatusNot(Integer userId, LocalDate date, LeaveStatus status);
}
