package com.crimeLink.analyzer.service;

import com.crimeLink.analyzer.dto.BulletAddDTO;
import com.crimeLink.analyzer.dto.BulletResponseDTO;
import com.crimeLink.analyzer.dto.BulletUpdateDTO;
import com.crimeLink.analyzer.entity.Bullet;

import java.util.List;

public interface BulletService {
    Bullet addBullet(BulletAddDTO dto);
    Bullet updateBullet(Integer bulletId, BulletUpdateDTO dto);
    List<Bullet> getAllBullets();
    List<BulletResponseDTO> getAllBulletsWithDetails();
    Bullet getBulletById(Integer bulletId);
}
