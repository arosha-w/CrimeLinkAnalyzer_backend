package com.crimeLink.analyzer.repository;

import com.crimeLink.analyzer.entity.Bullet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BulletRepository extends JpaRepository<Bullet, Integer> {
    Optional<Bullet> findByBulletId(Integer bulletId);
}