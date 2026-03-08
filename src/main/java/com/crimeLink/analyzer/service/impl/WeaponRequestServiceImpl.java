package com.crimeLink.analyzer.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.crimeLink.analyzer.dto.WeaponRequestDto;
import com.crimeLink.analyzer.entity.User;
import com.crimeLink.analyzer.entity.Weapon;
import com.crimeLink.analyzer.entity.WeaponRequest;
import com.crimeLink.analyzer.entity.WeaponRequestStatus;
import com.crimeLink.analyzer.mapper.WeaponRequestMapper;
import com.crimeLink.analyzer.repository.UserRepository;
import com.crimeLink.analyzer.repository.WeaponRepository;
import com.crimeLink.analyzer.repository.WeaponRequestRepository;
import com.crimeLink.analyzer.service.WeaponRequestService;

public class WeaponRequestServiceImpl implements WeaponRequestService {

    private final WeaponRequestRepository weaponRequestRepository;
    private final WeaponRepository weaponRepository;
    private final UserRepository userRepository;

    @Override
    public WeaponRequestDto createRequest(WeaponRequestDto requestDto) {
        Weapon weapon = weaponRepository.findById(requestDto.getWeaponSerial())
                .orElseThrow(() -> new RuntimeException("Weapon not Found"));

        User user = userRepository.findById(requestDto.getRequestedById())
                .orElseThrow(() -> new RuntimeException("User not Found"));

        WeaponRequest request = WeaponRequestMapper.mapToWeaponRequest(requestDto, weapon, user);

        request.setStatus(WeaponRequestStatus.PENDING);
        request.setRequestedAt(LocalDateTime.now());

        WeaponRequest saved = weaponRequestRepository.save(request);
        return WeaponRequestMapper.mapToWeaponRequestDto(saved);
    }

    @Override
    public List<WeaponRequestDto> getAllRequests() {
        return weaponRequestRepository.findAll().stream().map(WeaponRequestMapper::mapToWeaponRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public WeaponRequestDto approvedRequest(Integer requestId) {
        WeaponRequest request = weaponRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not Found"));

        request.setStatus(WeaponRequestStatus.APPROVED);
        request.setResolvedAt(LocalDateTime.now());

        return WeaponRequestMapper.mapToWeaponRequestDto(weaponRequestRepository.save(request));
    }

    @Override
    public WeaponRequestDto rejectedRequest(Integer requestId) {
        WeaponRequest request = weaponRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not Found"));

        request.setStatus(WeaponRequestStatus.REJECTED);
        request.setResolvedAt(LocalDateTime.now());

        return WeaponRequestMapper.mapToWeaponRequestDto(weaponRequestRepository.save(request));
    }

    @Override
    public List<WeaponRequestDto> getRequestsByUser(Integer userId) {
        return weaponRequestRepository.findByRequestedBy_UserId(userId).stream()
                .map(WeaponRequestMapper::mapToWeaponRequestDto).collect(Collectors.toList());
    }
}
