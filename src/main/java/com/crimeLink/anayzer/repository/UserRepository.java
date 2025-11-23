package com.crimeLink.anayzer.repository;

import com.crimeLink.anayzer.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByBadgeNo(String badgeNo);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByRoleAndStatus(String role, String status);
}
