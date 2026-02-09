package com.crimeLink.analyzer.service.impl;

import com.crimeLink.analyzer.dto.BulletAddDTO;
import com.crimeLink.analyzer.dto.BulletResponseDTO;
import com.crimeLink.analyzer.dto.BulletUpdateDTO;
import com.crimeLink.analyzer.entity.Bullet;
import com.crimeLink.analyzer.repository.BulletRepository;
import com.crimeLink.analyzer.service.BulletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BulletserviceImpl implements BulletService {

    private final BulletRepository bulletRepository;

    @Override
    @Transactional
    public Bullet addBullet(BulletAddDTO dto) {
        Bullet bullet = new Bullet();
        bullet.setBulletType(dto.getBulletType());
        bullet.setNumberOfMagazines(dto.getNumberOfMagazines());
        bullet.setRemarks(dto.getRemarks());
        
        return bulletRepository.save(bullet);
    }

    @Override
    @Transactional
    public Bullet updateBullet(Integer bulletId, BulletUpdateDTO dto) {
        Bullet bullet = bulletRepository.findById(bulletId)
                .orElseThrow(() -> new RuntimeException("Bullet not found with ID: " + bulletId));

        bullet.setBulletType(dto.getBulletType());
        bullet.setNumberOfMagazines(dto.getNumberOfMagazines());
        bullet.setRemarks(dto.getRemarks());

        return bulletRepository.save(bullet);
    }

    @Override
    public List<Bullet> getAllBullets() {
        return bulletRepository.findAll();
    }

    @Override
    public List<BulletResponseDTO> getAllBulletsWithDetails() {
        List<Bullet> bullets = bulletRepository.findAll();
        return bullets.stream()
                .map(this::convertToBulletResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Bullet getBulletById(Integer bulletId) {
        return bulletRepository.findById(bulletId)
                .orElseThrow(() -> new RuntimeException("Bullet not found with ID: " + bulletId));
    }

    private BulletResponseDTO convertToBulletResponseDTO(Bullet bullet) {
        BulletResponseDTO dto = new BulletResponseDTO();
        dto.setBulletId(bullet.getBulletId());
        dto.setBulletType(bullet.getBulletType());
        dto.setNumberOfMagazines(bullet.getNumberOfMagazines());
        dto.setRemarks(bullet.getRemarks());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (bullet.getRegisterDate() != null) {
            dto.setRegisterDate(bullet.getRegisterDate().format(dateFormatter));
        }

        return dto;
    }
}